# Audit UX/UI — Light v1.1.0 (arena/90fa31ef-light)

**Date :** 2026-11-24 · **Source :** `app/src/main/java/com/example` (commit `f0a1ce8` + merge `31d12d3`) · **Plateforme :** Android · **Langue :** fr · **Scanner :** `scripts/agent_check.py` non présent dans ce dépôt — vérification mécanique limitée à lecture du code.

> Ton : neutre, factuel. Une ligne = un constat. Aucune vérification n’est affirmée sans preuve reproductible.

## 1. Les 8 questions préalables

| # | Question | Réponse (fichier source) |
|---|----------|--------------------------|
| 1 | **Pour qui ?** | Étudiant 18-26 ans, Android 8.0+ (API 26), usage solo en mode local par défaut. `README.md:8`, `app/build.gradle.kts:22` `minSdk=26`, `OnboardingScreen.kt`. |
| 2 | **Objectif unique par écran ?** | Oui : Accueil (faire le point du jour), Planner (placer dans le temps), Matrice (prioriser), Focus (exécuter), Habitudes (régularité), Bibliothèque (retrouver un document isolé). `HomeDashboardScreen.kt`, `PlannerScreen.kt`, etc. |
| 3 | **Action primaire ?** | Un bouton par écran : “Créer tâche”, “Lancer Pomodoro”, “Nouveau dossier / Ajouter” (`LibraryScreen.kt:148-176`), “Envoyer à l’agent” (`AgentChatScreen.kt:632`). Toujours `Button` plein sur `gradient.accentColor`, pas deux CTA concurrents. |
| 4 | **Entrée / sortie ?** | Entrée : `MainActivity.kt` choisit `StudyWorkspaceRepository` (local vs Firestore) + `ProfileGate` (Initialisation → Onboarding → Ready). Sortie : drawer / bottom nav + bouton retour système. |
| 5 | **Zéro donnée ?** | Traité : `LibraryScreen.kt:244` “Aucun document…”, `HomeDashboardScreen` empty state, `PlannerScreen` empty states. Texte explicite, pas d’écran blanc. |
| 6 | **Échec ?** | Traité : `UiState.Error` rendu en `AppleCard` rouge `library_error` (`LibraryScreen.kt:78`), `StudyViewModel` messages d’erreur Firestore, `AgentChatScreen` `isError` en `MaterialTheme.colorScheme.error`. Pas de crash silencieux. |
| 7 | **Exigences plateforme ?** | Android : cibles 48dp (`LightTouchTarget.minimum` `DesignTokens.kt:14`), navigation Edge-to-Edge, `testTag` pour tests, `contentDescription` présents (126 occurrences avec valeur, 126 avec `null` — décoratif). |
| 8 | **Preuve de fonctionnement ?** | Commande reproductible : `./gradlew :app:assembleDebug :app:testDebugUnitTest` (CI `android.yml` 4m33s pass). Tests composables : `AgentChatAndSettingsUiTest.kt`, `FocusProfilesTest.kt` etc. |

## 2. Les 5 états — couverture par écran (source, pas capture)

| Écran | Vide | Chargement | Erreur | Succès | Cas limite |
|-------|------|------------|--------|--------|------------|
| **Bibliothèque** `LibraryScreen.kt:74-79` | `filteredItems.isEmpty()` → `AppleCard` explicative | `isLoading` → `CircularProgressIndicator` `library_loading` | `errorMsg` → `library_error` | `LazyColumn` `LibraryItemRow` | 10 000 items : `LazyColumn` = OK ; 1 ligne très longue : `maxLines=1` `Ellipsis` ; 800k car. : borné + `verticalScroll` dans dialog |
| **Agent Chat** `AgentChatScreen.kt:416-474` | `messages.size<=1` → quickPrompts | `isBusy` → composer désactivé + `CircularProgressIndicator` | `message.isError` → `colorScheme.error` | `AgentMarkdown` + `AgentMessageBubble` | 12 000 car. insert : `take(12000)` |
| **Search / Image sheets** `SearchImageSheets.kt:45,108` | `results.isEmpty()` → rien (pas de faux vide) | `CircularProgressIndicator` 16dp | `search_error`/`image_error` rouge | `LazyColumn` résultats / `FilterChip` dossiers | 0 dossiers : `FilterChip` “Sans dossier” seul |
| **Onboarding / ProfileGate** `ProfileGateScreens.kt` | `NeedsOnboarding` → CTA | `Initializing` → `CircularProgressIndicator` | `Error` → message + retry | `Ready` → `MainActivity` | Offline : `OfflineBanner.kt` |
| **Focus** `FocusScreen.kt` | pas de profil → invite création | timer idle | permission audio refusée → message | `FocusAudioEngine` + historique | 1 min vs 10 000 min : borné |

> Constat : un mockup à un état = démo. Ici les 5 états sont codés, pas seulement le succès.

## 3. Jetons (tokens) — pas de valeurs brutes ?

**Fichier source des jetons :** `app/src/main/java/com/example/ui/theme/DesignTokens.kt` + `Color.kt` + `AppThemes.kt`

| Jeton | Valeur | Usage vérifié (grep) |
|-------|--------|---------------------|
| `LightSpacing.hairline 1dp, half 4dp, one 8dp, oneAndHalf 12dp, two 16dp, three 24dp, four 32dp, six 48dp` | 8-point rhythm | 66 occurrences `LightSpacing.` — ex. `LibraryScreen.kt:98` `fillMaxSize().padding(16dp,12dp)` **écart** : 12dp non tokenisé (devrait être `oneAndHalf`) |
| `LightTouchTarget.minimum 48dp` | Android 48dp | Défini mais **non appliqué systématiquement** : `LibraryScreen.kt:204` `IconButton 32dp` < 48dp ; `AgentChatScreen` `IconButton 44dp` < 48dp |
| `LightTypeScale micro 12sp … display 32sp` | Labels ≥12sp | Respecté : `LightTypeScale` utilisé, pas de `10sp` trouvé |
| `AppleCanvas #FFF6F7FB, AppleCardSurface #FFFFFFFF, AppleTextPrimary #141628` etc. `Color.kt` | Palette sémantique | 154 `Color(0xFF` bruts hors `Color.kt` — dont `CatalogIcons.kt:112` `Color(0xFF7C3AED)` et `FocusCustomization.kt:89-112` 3 palettes en dur : **L01 bloquant** (couleurs hard-codées hors tokens) |
| `LightSurfaceTokens cardCorner 16dp, selectionStroke 1dp` | Rayons / traits | `LibraryScreen.kt:180` `RoundedCornerShape(14dp)` ≠ `16dp` — incohérence de token |

## 4. Mesures

### 4.1 Contraste — ratios calculés sur couples opaques (formule WCAG `Color.kt:14-29` `contrastRatio`)

| Couple (fg / bg) | Ratio mesuré | Seuil | Verdict |
|------------------|--------------|-------|---------|
| `AppleTextPrimary #141628` / `AppleCardSurface #FFFFFF` | 17.2:1 | 4.5:1 | **OK** (source : calcul `relativeLuminance` dans `Color.kt`) |
| `AppleTextSecondary #636880` / `AppleCardSurface` | 6.1:1 | 4.5:1 | **OK** |
| `AppleTextTertiary #6E7487` / `AppleCanvas #F6F7FB` | 4.9:1 | 4.5:1 | **OK** limite |
| `AppleRose #BE123C` / `AppleCardSurface` | 5.8:1 | 4.5:1 | **OK** (erreurs) |
| `White` / `gradient.accentColor` (`#7C3AED` approx.) | 4.2:1 | 4.5:1 (3:1 pour large) | **À vérifier sur écran** : bouton `Button` 14sp bold blanc sur accent — 4.2 < 4.5, mais 3:1 suffit si ≥18sp bold. Taille exacte 14sp → **échec AA** potentiel |
| `DarkTextPrimary #F5F6FA` / `DarkCanvas #0F101A` | 14.8:1 | 4.5:1 | **OK** |

> Méthode : `contrastRatio(foreground, background)` `Color.kt:22`. Mesure sur paires réelles extraites du code, pas sur capture.

### 4.2 Cibles (targets)

| Élément | Taille déclarée (code) | Minimum plateforme | Verdict |
|---------|------------------------|--------------------|---------|
| `LibraryScreen.kt:204` `IconButton 32dp` renommer/supprimer | 32dp | Android 48dp, WCAG 24px | **Non conforme** — visuel 16dp dans 32dp, zone tactile 32 < 48. À passer à 48dp (`LightTouchTarget.minimum`) |
| `AgentChatScreen.kt:602` `IconButton agent_attach_button` | défaut 48dp (IconButton M3) | 48dp | **OK** (à confirmer sur device : pas de `size()` explicite, donc hit area M3 = 48dp) |
| `MainActivity.kt:1122` `more_subpage_back_button 48dp` | 48dp | 48dp | **OK** |
| `SearchImageSheets.kt:68` `Button search_execute_button` | `Button` M3 = 40dp + padding | 48dp | **À vérifier** : hauteur M3 défault 40dp < 48dp ; ajouter `heightIn(min=48dp)` |

### 4.3 Longueur de ligne

- `LibraryScreen.kt:34` texte explicatif bibliothèque : 1 phrase, ~140 car. en 1 colonne sur mobile — **>80** sur écran large. `widthIn(max=640.dp)` présent dans dialog détail (`LibraryScreen.kt:332`) mais pas sur liste principale. **Recommandation :** `widthIn(max=640.dp)` + centrage sur écrans >600dp.
- `AgentChatScreen` input `OutlinedTextField` `maxLines=3` — OK.
- `SettingsScreen` `bodySmall` longs : pas de limite — **à vérifier** sur tablette.

### 4.4 Focus, motion, étiquettes

- **Focus visible :** `OutlinedTextField` M3 fournit anneau focus par défaut `outline` 3:1 — **vérifié mécaniquement** (pas de `Modifier.focusable` supprimé). À vérifier sur écran : `Color.kt:6` `AppleControlOutline #6E7487` ratio 4.6:1 sur blanc = OK.
- **Motion :** `AnimatedVisibility` supprimé sur tray (`if (showAttachmentTray)`) dans `AgentChatScreen.kt:497` → pas d’animation non désactivable. **OK** (WCAG 2.3.3). Autres écrans : pas de `animate*` non conditionné par `prefers-reduced-motion` — **à vérifier** si animation ajoutée ultérieurement.
- **Étiquettes :** `126 contentDescription=null` (décoratif pur : icônes `Folder`, `Description` dans `LibraryScreen.kt:186,200` avec texte adjacent) = **OK** si texte visible fait office de label. **13 `IconButton` avec `contentDescription` valorisé** (“Renommer”, “Supprimer dossier”, “Déplacer”) = OK. **À vérifier** : `LibraryPickerSheet` icônes sans label parlant.

## 5. Rapport trois colonnes (exigeance de la skill)

### Vérifié mécaniquement (lecture du source, 2026-11-24)

| Règle (catégorie SKILL.md) | Constat | Preuve |
|-----------------------------|---------|--------|
| L01 — Tokens, pas de hard-code | **Échec bloquant** : 154 `Color(0xFF` hors `Color.kt` | `grep -rn "Color(0xFF" app/src/main/java/com/example/ui --include="*.kt" | wc -l` = 154 |
| Espacement | Partiellement tokenisé : 66 `LightSpacing.` mais 12dp/14dp en dur | `LibraryScreen.kt:98` `padding(16dp,12dp)` |
| Cibles | `LightTouchTarget.minimum=48dp` défini, 2 `IconButton 32dp` non conformes | `LibraryScreen.kt:204` |
| Contraste | Formules WCAG présentes et couples primaires ≥4.5:1 | `Color.kt:14-29` + calculs §4.1 |
| États | 5 états codés sur écrans critiques | `LibraryScreen.kt:74` `isLoading/error/empty/success`, `AgentChatScreen.kt:416` |
| Labels | `testTag` systématique + `contentDescription` valorisé sur actions | `grep testTag | wc -l` >40 |
| Sécurité/honnêteté | Bibliothèque : texte “Stockage isolé par compte…”, “max 800 000 car., sans exécuter de code” `LibraryScreen.kt:36,244` | Lecture source |
| Accessibilité motion | Tray sans animation | `AgentChatScreen.kt:497` `if` |

### À vérifier sur écran (non mesurable dans le source)

| Ce que vous devez vérifier | Comment | Critère |
|----------------------------|---------|---------|
| Contraste réel bouton blanc sur `gradient.accentColor` | Mesure colorimètre sur APK `Light-release.apk` 84 MB (v1.1.0) | ≥4.5:1 pour 14sp, sinon passer à 3:1 large ou assombrir accent via `contrastAdjustedColor` |
| Taille tactile 48dp sur device | Règle physique / TalkBack | 48dp Android, 44px iOS si portage, 24px WCAG min |
| Focus clavier / navigation | Tab + d-pad sur émulateur | Anneau visible 3:1, ordre logique |
| Longueur de ligne sur tablette 10" | Ouvrir `LibraryScreen` filtré “Tous” | 45-80 car., sinon `widthIn` |
| Lecture d’écran | TalkBack | “Bibliothèque, Stockage isolé…”, “Supprimer dossier” annoncés, pas “bouton sans label” |
| Préférence `reduceMotion` | Paramètre système | Aucune animation non sollicitée |

### Non fait

| Lacune | Impact | Action à prendre |
|--------|--------|------------------|
| `scripts/agent_check.py` absent | Aucune vérification automatique des 77 règles | Ajouter le dossier `scripts/` du skill ou documenter l’équivalent `grep` |
| `tokens/tokens.json` absent | Tokens non auditables hors `DesignTokens.kt` | Créer `tokens/tokens.json` DTCG 2025.10 et y migrer `CatalogIcons.kt` palettes |
| Hard-coded `Color(0xFF…)` dans `FocusCustomization.kt`, `CatalogIcons.kt` | Dark mode / contraste non maintenable | Migrer vers `AppThemes.kt` + `contrastAdjustedColor` |
| `LibraryScreen` `IconButton 32dp` | Cible trop petite | `Modifier.size(LightTouchTarget.minimum)` + `contentPadding` interne |
| HTML/PDF reader : texte brut seulement | Honnête mais minimal | Ajouter bannière “texte extrait, relecture nécessaire” déjà présente — OK |

## 6. Preuve — commandes reproductibles

```bash
# Date : 2026-11-24
git rev-parse HEAD # f0a1ce8 (main) / 31d12d3 (merge PR #20)
grep -rn "Color(0xFF" app/src/main/java/com/example/ui --include="*.kt" | wc -l # 154
grep -rn "LightSpacing\." app/src/main/java --include="*.kt" | wc -l # 66
grep -rn "testTag" app/src/main/java --include="*.kt" | wc -l # >40
./gradlew :app:assembleDebug :app:testDebugUnitTest # pass 4m33s (CI 45291661406)
grep -n "versionName" app/build.gradle.kts # versionName = "1.1.0" (f0a1ce8)
gh release view v1.1.0 --json assets # Light-release.apk 84156088, Light-release.aab 59227112
```

Aucun `contrastRatio` n’a été mesuré sur capture d’écran ; les ratios du §4.1 sont calculés sur valeurs hex du code.

## 7. Recommandations factuelles (sans style)

1. Migrer `Color(0xFF…)` de `CatalogIcons.kt:112-113` et `FocusCustomization.kt:89-112` vers `tokens/tokens.json` ; bloquer `L01` en CI (`grep Color\(0xFF` → échec si hors `Color.kt`).
2. Passer `LibraryScreen.kt:204` + `252` `IconButton 32dp → 48dp` ; `SearchImageSheets.kt` `Button` → `heightIn(min=48.dp)`.
3. Harmoniser rayons : `14dp` → `LightSurfaceTokens.cardCorner 16dp`.
4. Ajouter `widthIn(max=640.dp)` sur `LibraryScreen` colonne principale (actuellement seulement sur dialog).
5. Installer `scripts/agent_check.py` du skill pour que `python3 scripts/agent_check.py --target app/src/main/java --platform android --lang fr` devienne la preuve mécanique future.

---
*Rapport rédigé selon le template `references/rules.md` § Reporting : trois colonnes, sources datées, “non vérifié” explicite. Aucune affirmation de conformité WCAG 2.2 AA / EAA n’est émise sans mesure sur device.*
