# Sécurité de la signature Android

Une ancienne version de ce fichier contenait une clé privée de signature Android
et son mot de passe. Ils restent accessibles dans l’historique Git : la clé doit
être considérée comme compromise, même si les valeurs ne figurent plus ici.

- Ne publiez aucun APK ou AAB signé avec l’ancienne clé.
- Générez une nouvelle clé et remplacez les secrets de signature dans GitHub.
- Mettez à jour les empreintes SHA-1 et SHA-256 dans Firebase et Play Console
  avant de télécharger une nouvelle configuration Firebase. Vérifiez également
  les restrictions et l’usage de l’ancienne clé API Firebase ; remplacez-la si
  nécessaire.
- Si l’ancienne clé était déjà associée à Google Play, suivez la procédure de
  remplacement de la clé d’importation dans Play Console.

Voir le [guide CI/CD](docs/CI_CD_SECRETS_GUIDE_FR.md) pour la procédure de publication.
