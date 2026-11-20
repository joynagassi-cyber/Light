# Light — Phase 0 : diagnostic Android et Firebase

**Gate 0 : non validé sur appareils.** Aucune phase 1–4 n’a été commencée. Les premiers changements de diagnostic ont été intégrés via la [PR #16](https://github.com/joynagassi-cyber/Light/pull/16). Light est une application **Android native (Kotlin + Jetpack Compose)**, avec Firebase Auth, Firestore et Firebase AI Logic ; elle n’utilise ni Ionic, ni Capacitor, ni Supabase. Cette PR de diagnostic n’inclut pas les travaux antérieurs non publiés sur la connexion et l’accueil initial.

## Preuves automatisées

- [CI Android #44653223038](https://github.com/joynagassi-cyber/Light/actions/runs/44653223038) : compilation APK, suite JVM (dont le test du bandeau clair/OLED) et signature de l’APK debug — **passées**.
- [Firestore Emulator #44653223407](https://github.com/joynagassi-cyber/Light/actions/runs/44653223407) : suite des règles, dont la création de profil propriétaire, les refus hors propriétaire et la synchro A → B puis B → A entre deux clients indépendants — **passée**.
- Les deux exécutions portent sur `f814007`. Leurs résultats ne valident ni une installation physique, ni Firestore en production, ni Firebase App Check.

## 0.1 — Installation Android

- **Fait, preuve :** `gh release view v1.0.0` confirme `Light-release.apk` (83 761 102 octets) et `SHA256SUMS.txt`. Le [job de publication signé](https://github.com/joynagassi-cyber/Light/actions/runs/44623906805) a réussi. `minSdk = 26` dans `app/build.gradle.kts`. Cela ne prouve pas une installation sur le téléphone.
- **Reproduction bloquée :** aucun `adb` ni appareil accessible dans ce workspace ; le [fichier officiel](https://github.com/joynagassi-cyber/Light/releases/tag/v1.0.0) n’a pas pu être rapatrié ici (EOF après redirection vers l’hébergeur des assets). Le seul symptôme communiqué est « package incorrect » lors d’une première installation. Aucun code `INSTALL_FAILED_*` du téléphone n’a été reçu. Le refus sur ce téléphone n’a pas été reproduit ; sa cause et un éventuel correctif APK ne sont pas établis. Ne pas réémettre ni incrémenter la release sur une hypothèse.
- **Test supplémentaire sur émulateur :** [workflow de vérification de l’APK publié](.github/workflows/published-apk-smoke.yml). Il teste l’APK public exact (SHA-256, certificat attendu, package et minSdk), puis l’installe sans `-r` sur un émulateur neuf et lance son activité principale. Deux cibles x86_64 : API 26 (minimum) et API 35 (cible de la build). La vérification ne reconstruit et ne publie rien, et ne valide pas la politique d’installation, l’architecture ou la version du téléphone de l’utilisateur.
- **Résultat (commit `efe54d2`) :** le [job Android 8 / API 26](https://github.com/joynagassi-cyber/Light/actions/runs/44661589089/job/136040856089) et le [job Android 15 / API 35](https://github.com/joynagassi-cyber/Light/actions/runs/44661589089/job/136040855760) ont tous deux passé la vérification de l’APK public, la première installation ADB et le démarrage de l’activité. Le même processus était encore actif après 8 secondes. Les traces des deux jobs sont jointes au [run #44661589089](https://github.com/joynagassi-cyber/Light/actions/runs/44661589089). Le premier run avait échoué avant l’installation à cause d’une empreinte de référence mal transcrite dans le harnais ; aucun changement n’a été apporté à l’APK.
- **Reproductibilité :** `ci/android_install.py` contrôle extension, archive, manifeste et SHA-256, sélectionne un appareil autorisé et restitue le code Android natif. Aucun désinstallateur ni effacement ; `--replace` est explicite. Les quatre cas du harnais sont passés avec `python3 -m unittest ci.test_android_install` (ADB simulé, pas un appareil).

PowerShell, depuis la racine du dépôt, avec Python 3.10+, Android Platform-Tools dans le `PATH` et le débogage USB autorisé :

```powershell
npm run android:install -- "$HOME\Downloads\Light-release.apk" --checksums "$HOME\Downloads\SHA256SUMS.txt"
```

Si plusieurs appareils sont connectés, ajouter `--serial <identifiant indiqué par adb devices>`. Conserver la ligne `Failure [INSTALL_FAILED_…]`, le modèle, la version Android et le SHA-256 ; ne pas transmettre le numéro de série.

## 0.2 — Faux bandeau hors ligne

- **Cause du faux message, démontrée par le code d’origine :** `MainActivity` agrégeait `UiState.Error` des huit flux Firestore (profil, matières, tâches, projets, sessions Focus, profils Focus, habitudes et échéances), puis affichait « Vérifiez votre connexion » sans consulter `isOnline`. Un refus Firestore ou un autre échec de données pouvait donc déclencher le bandeau avec Internet. **Le flux précis ayant déclenché le symptôme signalé reste inconnu** sans trace du téléphone.
- **Changement :** le bandeau `home_offline_banner` dépend exclusivement du réseau par défaut validé par Android (`INTERNET` et `VALIDATED`), pas des exceptions du backend. Le mode avion avec le Wi-Fi encore utilisable ne signifie pas nécessairement *hors ligne*. Le bandeau utilise les tokens existants et le cache comme explication, pas comme promesse de téléchargement. Les erreurs Firestore restent diagnostiquables séparément.
- **Validation :** test Compose en clair et en OLED (connecté → hors ligne → connecté) passé en CI. Reste le même parcours en conditions réelles, avec un défaut backend reproduit séparément pour vérifier qu’il n’est pas confondu avec le réseau.

## 0.3 — Refus de permission : périmètre et preuves manquantes

| Origine à distinguer | Constat / preuve à récupérer |
| --- | --- |
| Création ou lecture de profil Firestore | Le profil dépend du `uid` authentifié, des champs et bornes de `firestore.rules` ainsi que des règles *effectivement déployées* sur la base nommée dans `firebase.json`. Les règles testées localement ne prouvent pas la configuration de production. Relever le code et l’opération `create`/`get` sur `users/{id}`. |
| Firebase App Check / agent Gemini | Le code configure Play Integrity en release. Si la protection est imposée, un refus d’alignement entre certificat, canal et projet est une hypothèse à confirmer dans Firebase, pas une raison de désactiver le contrôle. Confirmer aussi que Firebase AI Logic est activé. |
| Fournisseur IA HTTP externe | `401` identifie un rejet d’authentification ; `403`, un refus d’accès côté fournisseur. Le client restitue ces codes avec des messages distincts sans afficher le corps d’erreur distant. Un défaut de droits précis ou de clé demeure à constater auprès du fournisseur. |
| Outil de l’agent | Les capacités du chat et les validations humaines sont des gardes applicatives distinctes d’un refus Android ou Firestore. Relever le nom de l’outil et distinguer refus de capacité, refus humain et échec de l’écriture. Ne pas élargir les capacités ou les règles par défaut. |
| Permissions Android | `INTERNET` et `ACCESS_NETWORK_STATE` sont déclarées dans le manifeste ; l’autorisation interactive `POST_NOTIFICATIONS` ne donne pas accès à Firestore ni aux fournisseurs IA. Aucun refus Android lié à ces parcours n’est reproduit ici. |

Le tag `FirestoreError` fournit désormais `code`, `operationType` et un chemin masqué, par exemple `users/{id}/tasks`. Cette journalisation ne contient ni UID, ni e-mail, ni valeur de clé. Sur le téléphone avec un APK de cette PR : `adb logcat -s FirestoreError:E`, puis reproduire l’opération concernée. Aucun secret ni contenu de chat à copier dans le rapport. Aucun changement des règles déployées ni d’App Check.

## 0.4 — Synchronisation

- **Fait :** suppression du badge de synchro visuel de l’accueil et des paramètres ; la synchronisation et le cache Firestore ne sont pas retirés.
- **Test écrit :** le test Firestore ajouté ouvre deux clients indépendants avec le même UID, attend une observation serveur d’une tâche créée dans chaque direction, puis vérifie qu’un autre UID ne peut pas les lire. Ce test émulateur, même vert, ne remplace pas deux téléphones ni la configuration de production. `npm ci` a réussi, mais le lancement local a échoué avant les tests : téléchargement du JAR `cloud-firestore-emulator-v1.19.8.jar` bloqué. Le workflow [Firestore rules and sync](.github/workflows/firestore.yml) exécute cette suite sur GitHub avec un projet `demo-no-project`, sans accès à la production.
- **Gate sur appareils :** connecter le même compte de test sur A et B avec l’APK de cette PR. Sur A, créer `SYNC-A-<heure>` ; vérifier son apparition sur B sans recréer la donnée. Inverser pour `SYNC-B-<heure>`. Noter le build, le sens, la durée et les résultats ; nettoyer les tâches du compte de test après mesure. Capturer l’accueil connecté, puis en mode avion avec Wi-Fi réellement coupé, puis après reconnexion, en clair et en OLED. Refaire une création de profil avec un compte de test neuf et relever les codes de refus s’il y en a.

## Décisions et risques

- Ne pas faire passer un test simulé pour un résultat d’installation. Ne pas masquer un refus de permission en le présentant comme un problème réseau.
- Pas de nouvelle fonctionnalité, pas de nouvelle valeur de style arbitraire ni de secret client introduit. La migration des paramètres en pages et le choix de bibliothèque de flou pour **Compose**, plutôt que WebView, restent en dehors de ce Gate.
- **Non fait :** reproduction sur le téléphone concerné, identification de la requête production fautive, création de profil réelle, validation agent/App Check et synchronisation sur deux appareils. La fusion du diagnostic ne valide pas le Gate 0. Aucune release supplémentaire n’a été publiée.

# Phase 1 — Surfaces et mode verre

**Gate 1 : non validé sur appareil réel.** Périmètre demandé par la suite : commencer par la phase 1, sans déclarer le Gate 0 validé. Pas de changement de formulaire, d’agent, de synchronisation ou de règles cloud ; phases 2 à 4 non commencées.

## Fait et cause identifiée

- `AppleCard` cumulait un fond translucide, un trait et une ombre, y compris pour les lignes de tâches. Le badge de catégorie et certaines cartes du calendrier ajoutaient un deuxième contour décoratif. Les sections de Paramètres imposaient à nouveau trait et ombre, avec un trait plus clair et plus coloré en mode verre. Enfin, le thème transformait globalement `surface`, `surfaceVariant` et `outlineVariant`, ce qui étendait cet effet aux composants qui n’avaient pas demandé de verre. La combinaison est visible dans le parent du commit de phase 1 ; son rendu physique n’a pas été capturé ici.
- `LightPanel`, `LightControl` et `LightSurfaceTokens` séparent désormais les cartes pleines, les contrôles sans bordure et les panneaux en verre. Le calendrier garde un contour quand il exprime une **sélection**, mais pas pour une carte inactive ou un simple badge de catégorie. Le réglage verre ne modifie plus les couleurs Material globales. Les contrôles de sélection de vue (pastille active et menu) utilisent le minimum partagé de **48 dp sur les deux axes**.
- La toile décorative (dégradé, halos, ruban) est la seule source échantillonnée. Le contraste et la saturation de cette source sont indépendants de ceux du texte. Un panneau de premier niveau applique un flou natif, un voile translucide et un seul reflet interne. Ses descendants ne ré-échantillonnent pas l’arrière-plan. Hors prise en charge (API < 31, rendu logiciel ou profil Android faible mémoire), le panneau conserve un fond plein, sans flou ni ombre. Si un appareil compatible reste trop lent, le réglage peut être désactivé ; aucune mesure de fluidité physique n’a encore été réalisée.

## Choix technique et alternatives

| Approche | Décision |
| --- | --- |
| **Jetpack Compose + Haze 1.1.1** | Retenu. Light est natif et utilise Compose 1.7.x. [La configuration de Haze à ce tag](https://github.com/chrisbanes/haze/blob/1.1.1/gradle/libs.versions.toml) cible Compose 1.7.5 et [son implémentation Android](https://github.com/chrisbanes/haze/blob/1.1.1/haze/src/androidMain/kotlin/dev/chrisbanes/haze/HazeChildNode.android.kt) applique un `RenderEffect` à une couche capturée. Une source décorative par écran ; aucun flou du texte ni de WebView. La saturation est appliquée aux accents du fond capturé, pas au contenu utilisateur. |
| **WebView avec CSS `backdrop-filter: blur(…) saturate(…)`** | Écarté. Ce filtre ne peut échantillonner que les éléments peints derrière dans le document Web. Il ne flouterait pas les vues Compose natives sous une WebView ; il faudrait répliquer ou migrer ces écrans et gérer un moteur de rendu supplémentaire. |
| **`Modifier.blur` appliqué à la carte** | Écarté. Ce modifier floute son propre calque, donc potentiellement le texte et les icônes, pas les contenus derrière la carte. Refaire une capture par panneau avec `RenderEffect` maison augmenterait la complexité du clipping, du défilement et du repli. |

## Vérifications et protocole physique restant

- **CI Android réussie** ([run 44662672903](https://github.com/joynagassi-cyber/Light/actions/runs/44662672903), commit `d4e446b`) : compilation debug et release, suite JVM et vérification de la signature debug. Les [rapports de tests](https://github.com/joynagassi-cyber/Light/actions/runs/44662672903/artifacts/14595594873) et l’[APK debug pour essai local](https://github.com/joynagassi-cyber/Light/actions/runs/44662672903/artifacts/14596910934) sont des artefacts CI temporaires, pas une nouvelle version publique. Si un APK signé autrement est déjà installé, utiliser un appareil de test distinct plutôt que désinstaller Light et risquer de perdre des données.
- Contrôles JVM ajoutés : état du repli (version Android, mémoire, accélération, préférence) et taille mesurée des deux contrôles du calendrier, avec ouverture du menu en clair et en OLED. Ils ne mesurent pas les pixels du flou ni la fluidité du GPU.
- **À faire sur appareil** : utiliser le même téléphone, mêmes données et même écran d’accueil/Planning avant et après. Capturer clair/sombre × verre désactivé/activé sur les deux builds (partir d’un jeu de tâches identique). Vérifier contours parasites, lisibilité, effet derrière la carte et absence de ré-application dans une carte imbriquée ; défiler rapidement en mode verre. Contrôler en plus le rendu plein sur un appareil API 30 ou faible mémoire. Joindre modèle, version Android, build et captures au rapport. Ne pas utiliser les émulateurs Phase 0 comme preuve physique du Gate 1.

# Phase 2 — Tâches, Planning, Gantt et Focus

**Démarrée sur demande explicite, sans validation rétroactive des Gates 0 et 1. Gate 2 non validé.**

| Point | Inventaire avant changement | Travaux de ce lot |
| --- | --- | --- |
| Création | Écran plein, deux CTA, options présentées en sections, aucune sous-tâche. | Feuille Material native partiellement ouverte et extensible, champ nom puis barre horizontale de raccourcis, popups avec valeur active et un seul CTA visible avant la liste. Jusqu’à huit sous-tâches, ordonnées via appui long/glissement ou actions d’accessibilité. Champ ordonné optionnel dans Firestore et dans le stockage local ; affiché dans les lignes de Planning. |
| Échéances / Gantt | Le formulaire contraignait les deux dates mais le plan IA ne vérifiait que leur forme ; tâches orphelines filtrées ; échelles Jour/Semaine/Mois/Trimestre et barres/jalons déjà présents. Aucun déplacement de tâche ni modèle de dépendances. | Le plan borne les dates à l’intervalle demandé. Les tâches dont le projet n’est pas disponible restent visibles hors projet. Un menu de ligne déplace le bloc via sélecteur de date, en gardant sa durée et en écrivant les deux dates ensemble. |
| Modèle actif | Le planificateur appelait directement un modèle Firebase fixé dans son dépôt. | Lit le fournisseur et le modèle configurés pour le chat, réutilise son transport HTTPS compatible sans copier son historique ni ses outils, ou la génération Firebase si ce fournisseur est actif. Message de configuration si la clé nécessaire manque ; pas de fallback silencieux. |
| Focus | Le minuteur partageait l’écran avec la navigation et les autres outils. | Une session en cours, sur l’onglet Focus, ne montre que le fond, la carte et une sortie. La sortie rend les contrôles sans arrêter la session ; l’écran reste éveillé pendant l’affichage de la session immersive seulement. |

### Preuves, limites et décisions

- **CI du commit `eafc82a` réussie** : [Android, builds debug/release et tests JVM](https://github.com/joynagassi-cyber/Light/actions/runs/44683751319), [règles et deux clients sur émulateur Firestore](https://github.com/joynagassi-cyber/Light/actions/runs/44683751495). Ces checks ne remplacent pas les essais physiques ni les appels aux fournisseurs réels.
- Tests ajoutés pour l’ordre et les limites des sous-tâches, la validation par les règles Firestore et le déplacement atomique. Aucun changement de règles déployé : **les sous-tâches ne sont pas acceptées en production tant que ces règles n’y sont pas déployées**.
- Position du CTA : il reste visible au-dessus de la liste dans la feuille mi-hauteur, plutôt que fixé au pied du contenu étendu. À vérifier avec le clavier sur petit écran. Le réordonnancement des sous-tâches s’applique au brouillon ; pas d’édition après création dans ce lot.
- **Non fait** : dépendances entre tâches, déplacement direct sur la frise par glisser-déposer, validation de tous les modes de rendu sur téléphone et essais réels de chaque fournisseur. Le transport externe repris du chat nécessite un endpoint compatible Chat Completions ; la prise en charge native Anthropic n’est pas démontrée. Les tâches aux dates illisibles restent exclues de la frise, sans migration des anciennes données.
- **Gate 2 sur téléphone** : créer une tâche et réordonner au moins trois sous-tâches, vérifier l’ordre après réouverture ; changer le modèle du chat, générer un plan puis réessayer sans clé ; déplacer une tâche de plusieurs jours, vérifier dates et barre à plusieurs échelles ; lancer Focus, observer l’écran éveillé au-delà du délai d’extinction et vérifier que la sortie ne stoppe pas le minuteur. Tester en clair et sombre. Pas de release ni de phase 3.

### Phase 2 — suite : interactions du Gantt

- **Déplacement sur la frise** : maintenir une barre ou un jalon, puis glisser. Jour déplace l’heure (tâche d’un jour ou jalon) ; Semaine/Mois déplacent d’un jour par colonne, Trimestre d’une semaine. Les déplacements conservent la durée ; le choix de date dans le menu reste disponible, notamment pour un bloc sur plusieurs jours en vue Jour.
- **Dépendances** : menu de tâche → « Dépendances… » → jusqu’à quatre prédécesseurs. Le Gantt signale les tâches ouvertes en amont, les dates incohérentes et les liens devenus introuvables. Les dépendances sont informatives : pas de recalcul automatique ou de propagation des déplacements. La sélection et l’enregistrement refusent les cycles dans le graphe chargé par le client. Firestore valide le format, l’unicité, l’absence d’auto-référence et l’existence chez le même propriétaire ; des écritures concurrentes sur plusieurs clients peuvent encore créer un cycle indirect.
- **Rendu** : les jalons n’apparaissent qu’à leur échéance ; la dernière journée d’un trimestre de 92 jours reste dans la dernière colonne. Tests ajoutés pour ces deux cas, le calcul des déplacements, les cycles directs/transitifs et les références Firestore (hors compte, supprimées, dupliquées, invalides).
- **Non vérifié sur téléphone** : tester un glissement réel et l’annulation du geste dans les quatre vues, la fluidité du défilement, puis vérifier les changements et les liens après redémarrage. Les nouvelles règles ne sont pas déployées. Pas de nouvelles migrations de l’ancien mode local vers le cloud, pas de phase 3 avant validation ou demande explicite.
- **CI du commit `8052e73` réussie** : [compilation Android debug/release, tests JVM et signature debug](https://github.com/joynagassi-cyber/Light/actions/runs/44709278111), [tests de règles et synchronisation sur émulateur Firestore](https://github.com/joynagassi-cyber/Light/actions/runs/44709277950). Le premier passage Android échouait sur une référence hors portée dans le dépôt local ; le décodage a été remis dans le lecteur de tâches et le passage suivant est vert. Ces résultats ne valident pas le geste sur téléphone.

### Phase 2 — validation ciblée avant essai physique

- Contrôles ajoutés : appui long puis déplacement d’une barre simulés par des événements tactiles Compose, annulation sans écriture, restauration de l’option écran éveillé en quittant la vue immersive et conservation des dates, des liens et de l’ordre des sous-tâches après réouverture du stockage local.
- Le planificateur normalise les dates, heures, durées et codes de cours avant l’aperçu. Un cours inconnu ne rattache plus la tâche au premier cours ; une réponse illisible ou une tâche sans titre déclenche un message clair sans afficher le contenu brut du fournisseur. Le transport externe est exercé avec un faux fournisseur, pas avec une clé réelle.
- **Critères sur téléphone** : développer la feuille et créer une tâche à trois sous-tâches ; fermer et rouvrir Light, puis vérifier l’ordre dans Planning. Dans le Gantt, déplacer une barre, annuler un second geste et vérifier que seule la première modification est conservée. Lier deux tâches et vérifier que le lien inverse est refusé dans l’interface. Dans Focus, attendre au-delà de la veille configurée, quitter la vue immersive et vérifier que le minuteur tourne toujours. Pour la planification IA, vérifier la génération après sélection d’un modèle du chat puis essayer sans clé (pour un fournisseur qui en exige une). Les essais distants avec sous-tâches/dépendances nécessitent un projet de test avec les règles de cette branche ; rien n’est déployé en production.
- **CI du commit `0d30c0e` réussie** : [builds Android, suite JVM/Compose et signature debug](https://github.com/joynagassi-cyber/Light/actions/runs/44712122474) et [émulateur Firestore](https://github.com/joynagassi-cyber/Light/actions/runs/44712122415). Le Gate 2 et les vérifications physiques des phases précédentes restent ouverts. Aucun déploiement, aucune publication.

### Phase 2 — complément sur le fournisseur IA actif

- Le planificateur utilise maintenant [`POST /v1/messages`](https://platform.claude.com/docs/en/api/messages/create) pour Anthropic, avec le modèle sélectionné, et conserve le format Chat Completions pour les fournisseurs compatibles. Firebase suit son chemin existant. Le planificateur n’envoie ni l’historique du chat ni ses outils ; aucune clé n’est ajoutée au code. Les tests de contrat utilisent des réponses fictives et vérifient aussi le refus d’une clé manquante avant tout appel.
- « Annuler » ferme l’état de chargement, conserve le brouillon et empêche une réponse tardive de remplacer l’état courant. Le fournisseur peut avoir déjà traité et facturé la requête ; l’annulation dans Light ne prétend pas annuler ce traitement à distance.
- Non vérifié : génération réelle sur Anthropic et les autres fournisseurs. Ce complément ne réécrit pas le chat Anthropic lui-même. Pas de changement de phase, de release ni de déploiement. Pour le Gate 2, suivre le parcours physique ci-dessus.
- **Preuves du commit `09adc18`** : [CI Android réussie](https://github.com/joynagassi-cyber/Light/actions/runs/44714127250), y compris le contrat du fournisseur, la compilation debug/release et la vérification de la signature debug. Les tests de contrat n’appellent aucun service réel.

# Phase 3 — Navigation, profil, réglages et bibliothèque

**Démarrée sur demande explicite. Les Gates physiques précédents ne sont pas validés rétroactivement ; Gate 3 non validé.**

### Premier lot : navigation et points d’entrée

- **Barre de navigation** : page dédiée dans Paramètres, réordonnancement par flèches accessibles, activation et quatre icônes au choix en plus de l’icône par défaut. Deux à quatre fonctionnalités visibles, Accueil obligatoire pour garder le chemin vers les réglages. Explorer reste fixe, donne accès aux fonctions masquées et ne contient ni Paramètres ni Capacités. La barre et le rail tablette partagent la même configuration.
- **Synchronisation** : document `users/{uid}/preferences/navigation`, privé au compte, liste fermée de routes et d’icônes. La dernière écriture validée remplace la disposition entière ; la mise en cache Firestore existante est utilisée pour les modifications hors ligne. En stockage local, la disposition survit au redémarrage. Les nouvelles règles sont à tester sur un projet de test avant déploiement ; aucune règle n’est publiée ici.
- **Profil et Capacités** : l’avatar d’Accueil et l’entrée Compte ouvrent la même page Profil, et non une feuille. La couverture par défaut est un dégradé déterministe par UID. Les capacités ont une page dans Paramètres et ne sont plus éditables dans Explorer ni dans la fenêtre de configuration du chat.

### Preuves, limites et décisions

- Tests prévus pour l’ordre, l’icône, les limites et la persistance locale ; scénario émulateur à deux clients du même compte, dont une écriture hors ligne, refus d’un autre compte et du stockage d’une clé. Ces contrôles ne remplacent pas l’essai physique.
- **Non fait dans ce lot** : conversion des autres entrées héritées de Paramètres en pages, import de couverture et Unsplash avec proxy/attribution/tracking, liens de création de clés et réglages image, bibliothèque cloud et rendu isolé des artefacts (Markdown/PDF/HTML). Pas d’intégration Unsplash/Agnes sur la base d’une URL ou d’un secret supposé. Phase 4 non démarrée.
- **À vérifier sur téléphone pour ce lot** : réordonner deux fonctions, en masquer une, changer d’icône ; vérifier le résultat sur un second téléphone du même compte, puis modifier hors ligne et rouvrir Light. Depuis l’avatar et Paramètres, vérifier le même profil en clair et sombre et les retours vers le bon écran. Le Gate 3 complet attend aussi les éléments non faits ci-dessus.
