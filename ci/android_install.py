#!/usr/bin/env python3
"""Install one APK on an explicitly selected Android device without hiding adb failures."""
import argparse
import hashlib
import re
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path


def inspect_apk(apk: Path, sums: Path | None) -> str:
    if apk.suffix.lower() != '.apk' or not apk.is_file():
        raise ValueError('Choisissez un fichier .apk existant (un .aab ne s’installe pas directement).')
    if not zipfile.is_zipfile(apk):
        raise ValueError('Le fichier n’est pas une archive APK lisible.')
    with zipfile.ZipFile(apk) as archive:
        if 'AndroidManifest.xml' not in archive.namelist() or archive.testzip() is not None:
            raise ValueError('APK invalide : manifeste absent ou archive endommagée.')
    digest = hashlib.sha256()
    with apk.open('rb') as src:
        for chunk in iter(lambda: src.read(1024 * 1024), b''):
            digest.update(chunk)
    actual = digest.hexdigest()
    if sums is not None:
        matches = [m.group(1).lower() for line in sums.read_text(encoding='utf-8-sig').splitlines()
                   if (m := re.fullmatch(r'([a-fA-F0-9]{64})\s+[ *]?(.*)', line)) and m.group(2) == apk.name]
        if len(matches) != 1:
            raise ValueError(f'Aucune somme SHA-256 unique pour {apk.name} dans {sums.name}.')
        if matches[0] != actual:
            raise ValueError(f'SHA-256 différent du fichier de référence : {actual}. Retéléchargez l’APK.')
    return actual


def adb_serial(output: str, chosen: str | None) -> str:
    devices = {}
    for line in output.splitlines():
        match = re.fullmatch(r'(\S+)\s+(device|offline|unauthorized|recovery|sideload|bootloader)\s*', line.strip())
        if match:
            devices[match.group(1)] = match.group(2)
    if chosen is not None and devices.get(chosen) == 'device':
        return chosen
    if chosen is not None:
        raise ValueError('L’appareil sélectionné est absent ou non autorisé.')
    if len(devices) != 1 or 'device' not in devices.values():
        raise ValueError('Connectez et autorisez un seul appareil (ou utilisez --serial avec adb devices).')
    return next(iter(devices))


def run(args) -> int:
    print(f'APK : {args.apk.name}\nSHA-256 : {inspect_apk(args.apk, args.checksums)}', flush=True)
    adb = shutil.which(args.adb)
    if adb is None:
        raise ValueError('adb introuvable. Installez Android SDK Platform-Tools ou utilisez --adb CHEMIN.')
    devices = subprocess.run([adb, 'devices'], check=True, capture_output=True, text=True, timeout=30)
    serial = adb_serial(devices.stdout, args.serial)
    result = subprocess.run([adb, '-s', serial, 'install', *(['-r'] if args.replace else []), str(args.apk.resolve())],
                            capture_output=True, text=True, errors='replace', timeout=300)
    print(result.stdout, end='')
    print(result.stderr, end='', file=sys.stderr)
    if result.returncode or not any(line.strip() == 'Success' for line in result.stdout.splitlines()):
        return result.returncode or 1
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('apk', type=Path)
    parser.add_argument('--checksums', type=Path, help='Fichier SHA256SUMS.txt téléchargé avec l’APK.')
    parser.add_argument('--adb', default='adb', help='Chemin optionnel de adb.')
    parser.add_argument('--serial', help='Numéro renvoyé par adb devices si plusieurs appareils sont connectés.')
    parser.add_argument('--replace', action='store_true', help='Autorise la mise à jour sans effacer les données.')
    try:
        return run(parser.parse_args())
    except (ValueError, OSError, zipfile.BadZipFile, subprocess.SubprocessError) as error:
        print(f'Installation arrêtée : {error}', file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
