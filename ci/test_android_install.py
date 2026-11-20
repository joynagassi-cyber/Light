import contextlib
import hashlib
import io
import subprocess
import tempfile
import unittest
import zipfile
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch

from ci.android_install import adb_serial, inspect_apk, run


class InstallTest(unittest.TestCase):
    def setUp(self):
        self.dir = tempfile.TemporaryDirectory()
        self.addCleanup(self.dir.cleanup)
        self.apk = Path(self.dir.name) / 'Light-release.apk'
        with zipfile.ZipFile(self.apk, 'w') as archive:
            archive.writestr('AndroidManifest.xml', 'fixture only')

    def test_hash_mismatch_stops_install(self):
        sums = self.apk.with_name('SHA256SUMS.txt')
        sha = hashlib.sha256(self.apk.read_bytes()).hexdigest()
        sums.write_text(f'{sha}  {self.apk.name}\n')
        self.assertEqual(inspect_apk(self.apk, sums), sha)
        sums.write_text(f'{"0" * 64}  {self.apk.name}\n')
        with patch('subprocess.run') as adb, self.assertRaisesRegex(ValueError, 'SHA-256 différent'):
            run(self.args(checksums=sums))
        adb.assert_not_called()

    def test_aab_is_rejected_without_adb(self):
        self.apk.rename(self.apk.with_suffix('.aab'))
        with self.assertRaisesRegex(ValueError, 'un fichier .apk'):
            inspect_apk(self.apk.with_suffix('.aab'), None)

    def test_requires_unambiguous_authorized_device(self):
        devices = 'List of devices attached\nA\tdevice\nB\tunauthorized\n'
        with self.assertRaisesRegex(ValueError, 'un seul appareil'):
            adb_serial(devices, None)
        with self.assertRaisesRegex(ValueError, 'non autorisé'):
            adb_serial(devices, 'B')
        self.assertEqual(adb_serial(devices, 'A'), 'A')

    def test_retains_android_failure_without_destructive_action(self):
        calls = [subprocess.CompletedProcess([], 0, 'List of devices attached\nA\tdevice\n', ''),
                 subprocess.CompletedProcess([], 1, '', 'Failure [INSTALL_FAILED_INVALID_APK]\n')]
        with patch('shutil.which', return_value='adb'), patch('subprocess.run', side_effect=calls) as adb, \
             contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()) as stderr:
            self.assertEqual(run(self.args()), 1)
        self.assertIn('INSTALL_FAILED_INVALID_APK', stderr.getvalue())
        self.assertEqual(adb.call_args.args[0], ['adb', '-s', 'A', 'install', str(self.apk)])

    def args(self, **options):
        return SimpleNamespace(**{'apk': self.apk, 'checksums': None, 'adb': 'adb', 'serial': None, 'replace': False, **options})
