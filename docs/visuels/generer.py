#!/usr/bin/env python3
"""Régénère les illustrations SVG du README, sans dépendance externe."""
from html import escape
from pathlib import Path
import sys

OUT = Path(__file__).resolve().parent
CHECK = '--check' in sys.argv
INK = '#F7F8FF'
MUTED = '#C0CBE1'
DIM = '#9BAAC6'
BG = '#101728'
PANEL = '#171F35'
BORDER = '#36445F'
VIOLET = '#B9A5FF'
MINT = '#87F4D0'
BLUE = '#9EC9FF'
AMBER = '#FFD293'
PINK = '#F8ADD4'
SANS = 'Inter, system-ui, -apple-system, Segoe UI, sans-serif'
MONO = 'ui-monospace, SFMono-Regular, Consolas, monospace'


def rect(x, y, w, h, fill=PANEL, stroke=BORDER, r=14, opacity=1):
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="{stroke}" opacity="{opacity}"/>'


def text(x, y, value, size=17, color=INK, weight=550, anchor='start', mono=False, spacing=0):
    font = MONO if mono else SANS
    return (f'<text x="{x}" y="{y}" font-family="{font}" font-size="{size}" '
            f'fill="{color}" font-weight="{weight}" text-anchor="{anchor}" '
            f'letter-spacing="{spacing}">{escape(value)}</text>')


def line(x1, y1, x2, y2, color=BORDER, width=1, dash=''):
    return (f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" '
            f'stroke-width="{width}" stroke-dasharray="{dash or "none"}" stroke-linecap="round"/>')


def circle(x, y, radius, fill, stroke='none', width=1):
    return f'<circle cx="{x}" cy="{y}" r="{radius}" fill="{fill}" stroke="{stroke}" stroke-width="{width}"/>'


def path(d, color, width=2, fill='none'):
    return f'<path d="{d}" stroke="{color}" stroke-width="{width}" fill="{fill}" stroke-linecap="round" stroke-linejoin="round"/>'


def arrow(x1, y1, x2, y2, color=DIM):
    return line(x1, y1, x2, y2, color, 2) + path(f'M{x2-6} {y2-4}l6 4-6 4', color, 2)


def pill(x, y, w, label, color=VIOLET):
    return rect(x, y, w, 28, BG, BORDER, 14) + circle(x+14, y+14, 3, color) + text(x+25, y+19, label, 13, color, 650)


def surface(x, y, w, h, label=None):
    out = rect(x, y, w, h)
    if label:
        out += text(x+16, y+25, label, 13, DIM, 700, mono=True, spacing=1)
    return out


def svg(name, title, desc, height, content, defs=''):
    markup = (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="{height}" viewBox="0 0 1080 {height}" '
        'role="img" aria-labelledby="title desc">\n'
        f'  <title id="title">{escape(title)}</title>\n'
        f'  <desc id="desc">{escape(desc)}</desc>\n'
        + (f'  <defs>{defs}</defs>\n' if defs else '')
        + '  ' + content.replace('><', '>\n  <') + '\n</svg>\n'
    )
    output = OUT / f'{name}.svg'
    if CHECK:
        if not output.is_file() or output.read_text(encoding='utf-8') != markup:
            raise SystemExit(f'SVG manquant ou à régénérer : {output.name}')
    else:
        output.write_text(markup, encoding='utf-8')


def feature(name, number, title, lines, caption, drawing, accent=VIOLET):
    title_size = 34 if len(title) <= 16 else 29 if len(title) <= 20 else 27
    base = rect(1, 1, 1078, 258, BG, BORDER, 22)
    base += line(376, 30, 376, 230) + circle(43, 43, 4, accent)
    base += text(56, 48, f'LIGHT / {number}', 14, accent, 750, mono=True, spacing=2)
    base += text(36, 105, title, title_size, INK, 760)
    base += ''.join(text(38, 146+i*24, label, 18, MUTED) for i, label in enumerate(lines))
    base += text(38, 224, caption, 12, accent, 720, mono=True, spacing=0.75)
    svg(name, title, ' '.join(lines), 260, base + drawing)


def hero():
    defs = ('<linearGradient id="night" x2="1" y2="1" gradientUnits="objectBoundingBox">'
            '<stop stop-color="#13182F"/><stop offset=".65" stop-color="#222344"/>'
            '<stop offset="1" stop-color="#192D44"/></linearGradient>'
            '<linearGradient id="beam"><stop stop-color="#C7ADFF"/><stop offset="1" stop-color="#87F4D0"/></linearGradient>'
            '<pattern id="dot" width="22" height="22" patternUnits="userSpaceOnUse">'
            '<circle cx="2" cy="2" r="1" fill="#B9A5FF" opacity=".34"/></pattern>'
            '<clipPath id="bounds"><rect x="1" y="1" width="1078" height="356" rx="30"/></clipPath>')
    body = rect(1, 1, 1078, 356, 'url(#night)', BORDER, 30)
    body += ('<g clip-path="url(#bounds)">' + rect(550, 0, 530, 358, 'url(#dot)', 'none', 0)
             + circle(945, 155, 184, 'none', '#596398') + circle(945, 155, 202, 'none', '#464F76')
             + path('M550 330 Q740 390 1010 52', 'url(#beam)', 2) + '</g>')
    body += rect(48, 43, 42, 42, '#322E55', '#7665A7', 12)
    body += path('M70 51 Q71 61 82 64 Q71 66 70 77 Q67 66 58 64 Q67 61 70 51Z', 'none', 0, 'url(#beam)')
    body += text(103, 69, 'WORKSPACE D’ÉTUDE  /  ANDROID', 14, MINT, 750, mono=True, spacing=1.5)
    body += text(47, 171, 'Light', 103, INK, 800, spacing=-6)
    body += text(51, 219, 'Un espace pour étudier avec clarté.', 30, '#E4DCFF', 650)
    body += text(52, 258, 'Planifier ses tâches, se concentrer, suivre ses progrès.', 18, MUTED)
    body += pill(52, 292, 99, 'Kotlin', VIOLET) + pill(162, 292, 113, 'Compose', MINT) + pill(286, 292, 136, 'Mode local', BLUE)
    body += rect(632, 59, 330, 226, '#121B31', '#6875A7', 23)
    body += text(658, 93, 'MON ESPACE', 14, MINT, 750, mono=True, spacing=1.4)
    body += text(658, 122, 'Une session à la fois', 22, INK, 700)
    body += line(658, 137, 934, 137)
    for i, (label, color, length) in enumerate((('Planifier', VIOLET, 103), ('Se concentrer', MINT, 132), ('Faire le bilan', BLUE, 71))):
        y = 162 + i*35
        body += circle(667, y+5, 4, color) + text(682, y+10, label, 15, MUTED) + rect(793, y+1, 140, 8, '#2A3451', 'none', 4)
        body += rect(793, y+1, length, 8, color, 'none', 4)
    body += rect(869, 246, 150, 56, '#282447', '#A78DEB', 16)
    body += text(888, 281, '25:00', 29, INK, 750, mono=True) + circle(992, 274, 10, MINT)
    body += path('M990 270l6 4-6 4z', BG, 1, BG)
    svg('light', 'Light — espace d’étude Android', 'Planifier, se concentrer et suivre sa progression. Le visuel est une illustration, pas une capture.', 358, body, defs)


def architecture():
    out = rect(1, 1, 1078, 368, BG, BORDER, 22)
    out += text(34, 44, 'LIGHT / ARCHITECTURE', 14, MINT, 740, mono=True, spacing=1.6)
    out += text(34, 79, 'Une interface, deux modes de données', 28, INK, 740)
    labels = [(36, '01 · INTERFACE'), (300, '02 · ÉTAT'), (555, '03 · CONTRAT'), (841, '04 · STOCKAGE')]
    for x, label in labels:
        out += text(x, 123, label, 14, DIM, 700, mono=True, spacing=0.4)
    for x, w, color, title, desc in (
        (36, 207, VIOLET, 'Jetpack Compose', 'MainActivity · écrans'),
        (299, 201, BLUE, 'StudyViewModel', 'Actions · état UI'),
        (554, 248, MINT, 'StudyWorkspaceRepository', 'Opérations métier · Flow'),
    ):
        out += rect(x, 153, w, 84) + rect(x+14, 169, 5, 9, color, 'none', 2)
        out += text(x+15, 197, title, 16, INK, 730) + text(x+15, 219, desc, 14, MUTED)
    out += arrow(254, 195, 288, 195) + arrow(510, 195, 543, 195)
    out += path('M803 195H822V161H833M822 195V236H833', DIM, 2)
    out += path('M829 157l6 4-6 4M829 232l6 4-6 4', DIM, 2)
    for y, title, desc, color in ((132, 'Mode local', 'Fichier JSON privé', MINT), (207, 'Mode connecté', 'Firestore + cache', VIOLET)):
        out += rect(841, y, 202, 59)
        out += circle(855, y+20, 3, color) + text(866, y+24, title, 16, INK, 700) + text(855, y+45, desc, 14, MUTED)
    out += rect(36, 288, 1007, 46, '#1B2740', '#40536C', 12)
    out += text(52, 317, 'EN PARALLÈLE', 13, MINT, 750, mono=True, spacing=0.7)
    out += text(198, 317, 'DataStore : réglages et fils IA', 16, MUTED) + line(470, 300, 470, 323)
    out += text(495, 317, 'Firebase Auth / App Check et fournisseurs IA : selon la configuration', 15, MUTED)
    svg('architecture', 'Architecture Android de Light', 'L’interface Compose envoie des actions au ViewModel. Le contrat StudyWorkspaceRepository permet de choisir entre un stockage JSON local et Firestore en mode connecté. DataStore gère les préférences et les fils de l’agent.', 370, out)


def overview():
    out = surface(404, 33, 354, 194, 'AUJOURD’HUI')
    for i, (label, color) in enumerate((('Réviser le chapitre 4', VIOLET), ('Pratiquer les exercices', MINT), ('Préparer la prochaine séance', BLUE))):
        y = 88+i*48
        out += line(420, y+20, 742, y+20) if i < 2 else ''
        out += circle(437, y, 9, BG, color, 2) + text(456, y+6, label, 16, INK)
    out += surface(773, 33, 273, 194, 'OBJECTIF DU JOUR')
    out += text(793, 118, '2 / 3', 49, INK, 760) + text(797, 147, 'tâches terminées', 16, MUTED)
    out += rect(794, 176, 228, 10, '#33415E', 'none', 5) + rect(794, 176, 152, 10, MINT, 'none', 5)
    return out


def planner():
    out = surface(404, 33, 642, 194, 'CALENDRIER')
    for i, label in enumerate(('Jour', 'Semaine', 'Mois')):
        out += pill(756+i*91, 44, 85, label, MINT if i == 1 else DIM)
    for i, label in enumerate(('LUN', 'MAR', 'MER', 'JEU', 'VEN', 'SAM', 'DIM')):
        x = 420+i*87
        out += rect(x, 90, 83, 112, '#11182C', '#293A56', 8)
        out += text(x+41, 111, label, 12, MUTED, 700, 'middle', True)
        if i in (0, 2, 3, 4):
            color = [VIOLET, BLUE, MINT, AMBER][(0, 2, 3, 4).index(i)]
            y = 128+10*(i%4)
            out += rect(x+6, y, 71, 26, color, 'none', 6)
            out += text(x+41, y+18, ['09:00', '14:00', '10:30', '16:00'][(0, 2, 3, 4).index(i)], 13, BG, 770, 'middle', True)
    return out


def matrix():
    out = text(754, 49, 'URGENT', 13, DIM, 720, 'middle', True) + text(940, 49, 'NON URGENT', 13, DIM, 720, 'middle', True)
    for y, label in ((100, 'IMPORTANT'), (176, 'NON IMPORTANT')):
        out += text(638, y+10, label, 13, DIM, 720, 'end', True)
    for x, y, label, color in ((660, 60, 'Prioriser', PINK), (850, 60, 'Planifier', MINT), (660, 137, 'Alléger', AMBER), (850, 137, 'Reporter', BLUE)):
        out += rect(x, y, 178, 68) + circle(x+21, y+34, 5, color) + text(x+36, y+40, label, 17, INK, 700)
    return out + path('M428 96h75m-12-8 12 8-12 8', VIOLET, 2) + text(428, 143, 'Une tâche', 16, MUTED)


def gantt():
    out = surface(404, 33, 642, 194, 'CHRONOLOGIE DES PROJETS')
    for i in range(6):
        x = 556+i*78
        out += text(x, 82, str(12+i), 13, DIM, 650, 'middle', True) + line(x, 91, x, 205)
    for i, (label, start, width, color) in enumerate((('Recherche', 0, 126, BLUE), ('Réalisation', 82, 181, VIOLET), ('Révision', 237, 102, MINT))):
        y = 100+i*36
        out += text(424, y+17, label, 15, MUTED) + rect(551+start, y, width, 23, color, 'none', 7)
    out += path('M949 166l10 10-10 10-10-10z', 'none', 0, AMBER) + text(947, 202, 'Jalon', 12, AMBER, 650, 'middle')
    return out


def timer():
    out = surface(404, 33, 642, 194)
    out += circle(522, 129, 67, 'none', '#374663', 8) + path('M522 62a67 67 0 1 1 -60 96', MINT, 8)
    out += text(522, 139, '25:00', 28, INK, 750, 'middle', True)
    out += line(618, 58, 618, 200)
    out += text(651, 83, 'Choisir un mode', 17, INK, 700)
    out += pill(652, 96, 116, 'Pomodoro', MINT) + pill(779, 96, 117, 'Deep Code', VIOLET)
    out += pill(652, 136, 116, 'Pause', AMBER) + pill(779, 136, 117, 'Chrono', BLUE)
    out += text(654, 194, 'Session → historique Focus', 16, MUTED)
    return out


def focus_profiles():
    out = ''
    for i, (title, desc, color) in enumerate((('Profil Focus', 'Sujet à approfondir', VIOLET), ('Tâche', 'Activité liée au profil', BLUE), ('Session', 'Durée enregistrée', MINT))):
        x = 404+i*221
        out += surface(x, 76, 200, 108, f'0{i+1}')
        out += text(x+17, 132, title, 23, color, 740) + text(x+17, 159, desc, 14, MUTED)
        if i < 2:
            out += arrow(x+204, 127, x+216, 127, color)
    return out


def focus_analytics():
    out = surface(404, 33, 365, 194, 'MINUTES PAR JOUR') + surface(782, 33, 264, 194, 'RÉPARTITION')
    for y in (90, 121, 152, 183):
        out += line(431, y, 741, y)
    out += path('M431 176L481 153 533 165 585 123 636 140 687 101 739 115', MINT, 4)
    for x, y in ((431, 176), (481, 153), (533, 165), (585, 123), (636, 140), (687, 101), (739, 115)):
        out += circle(x, y, 4, MINT)
    for x, letter in zip(range(431, 742, 51), 'LMMJVSD'):
        out += text(x, 208, letter, 13, DIM, 660, 'middle', True)
    out += circle(851, 136, 40, 'none', '#384763', 12)
    out += path('M851 96a40 40 0 1 1 -39 50', VIOLET, 12) + path('M811 139a40 40 0 0 1 15-35', BLUE, 12)
    out += text(915, 123, 'Modes', 17, INK, 680) + text(915, 151, 'Profils', 17, MUTED)
    return out


def progress():
    out = surface(404, 33, 312, 194, 'OBJECTIFS PAR MATIÈRE') + surface(730, 33, 316, 194, 'ÉQUILIBRE')
    for i, (label, width, color) in enumerate((('Algorithmique', 191, VIOLET), ('Architecture', 144, MINT), ('Systèmes', 103, BLUE))):
        y = 82+i*44
        out += text(422, y, label, 14, MUTED) + rect(422, y+8, 269, 9, '#34445E', 'none', 4) + rect(422, y+8, width, 9, color, 'none', 4)
    out += path('M887 72l58 42-22 68h-72l-22-68zM887 95l36 26-14 42h-44l-14-42z', BORDER, 1)
    out += path('M887 72v62m58-20-58 20m36 48-36-48m-36 48 36-48m-58-20 58 20', BORDER, 1)
    out += path('M887 87l40 34-14 44-46-3-24-44z', VIOLET, 3, '#343258')
    out += text(887, 210, 'Radar de maîtrise', 13, MUTED, 650, 'middle')
    return out


def habits():
    out = surface(404, 33, 642, 194, 'RITUELS DE LA SEMAINE')
    for i, label in enumerate(('L', 'M', 'M', 'J', 'V', 'S', 'D')):
        out += text(633+i*54, 92, label, 13, DIM, 720, 'middle', True)
    for j, label in enumerate(('Lecture', 'Révision', 'Exercices')):
        y = 118+j*38
        out += text(428, y+6, label, 17, INK)
        for i in range(7):
            x = 633+i*54
            done = i < 5 and (i+j*2)%4 != 1
            out += rect(x-12, y-12, 24, 24, MINT if done else BG, 'none' if done else BORDER, 7)
            if done:
                out += path(f'M{x-5} {y}l3 4 7-8', BG, 2)
    return out


def countdown():
    out = surface(404, 33, 642, 194, 'PROCHAIN JALON')
    out += text(431, 150, '05', 72, AMBER, 800, mono=True)
    out += text(434, 183, 'JOURS RESTANTS', 13, MUTED, 680, mono=True, spacing=1)
    out += line(593, 70, 593, 197) + text(626, 108, 'Partiel d’algorithmique', 23, INK, 740)
    out += pill(627, 127, 106, 'Examen', AMBER) + pill(743, 127, 131, 'Date cible', VIOLET)
    out += text(628, 183, 'Examens  /  Projets  /  Événements', 15, MUTED)
    return out


def search():
    out = surface(404, 33, 642, 194)
    out += rect(423, 49, 601, 44, BG, '#5A6E99', 11)
    out += circle(446, 69, 7, 'none', BLUE, 2) + line(451, 75, 456, 80, BLUE, 2)
    out += text(470, 78, 'algorithmique', 19, INK, 630)
    for i, (label, color, w) in enumerate((('Tâches', VIOLET, 101), ('Cours', BLUE, 89), ('Habitudes', MINT, 121), ('Échéances', AMBER, 122))):
        out += pill([424, 536, 636, 769][i], 106, w, label, color)
    out += line(423, 150, 1022, 150) + text(431, 179, 'Réviser les graphes', 17, INK) + text(431, 205, 'Résultat · Tâche', 13, DIM)
    return out


def pipeline(cards, color):
    out = ''
    for i, (title, desc) in enumerate(cards):
        x = 403+i*165
        out += surface(x, 71, 146, 118, f'0{i+1}')
        out += text(x+14, 132, title, 17, color, 740) + text(x+14, 157, desc, 13, MUTED)
        if i < len(cards)-1:
            out += arrow(x+150, 130, x+161, 130, color)
    return out


def agent():
    out = surface(404, 33, 303, 194, 'CONVERSATION')
    out += rect(420, 70, 268, 37, '#232E48', 'none', 10) + text(433, 94, 'Prépare mes révisions.', 15, INK)
    out += rect(420, 117, 268, 47, '#30294D', 'none', 10) + text(433, 140, 'Proposition : créer une tâche', 15, INK)
    out += text(434, 152+40, 'Fils · Fournisseurs · Capacités', 13, DIM)
    out += arrow(714, 130, 734, 130, VIOLET) + surface(741, 33, 305, 194, 'ACTION CONTRÔLÉE')
    out += text(757, 98, 'Créer « Réviser les graphes »', 17, INK, 720)
    out += text(757, 126, 'Aucune écriture avant accord.', 15, MUTED)
    out += pill(759, 152, 114, 'Autoriser', MINT) + pill(886, 152, 104, 'Refuser', PINK)
    return out


def appearance():
    out = surface(404, 33, 309, 194, 'CONFIGURATION INITIALE') + surface(726, 33, 320, 194, 'APPARENCE')
    for i, (label, color) in enumerate((('Profil et objectifs', VIOLET), ('Habitudes suggérées', MINT), ('Préférences de rappel', BLUE))):
        y = 89+i*41
        out += circle(430, y, 7, BG, color, 2) + text(448, y+6, label, 16, INK)
        if i < 2:
            out += line(430, y+12, 430, y+29)
    for i, color in enumerate((VIOLET, MINT, AMBER, BLUE, PINK)):
        out += rect(746+i*57, 85, 43, 42, color, 'none', 11)
    out += pill(747, 151, 81, 'Clair', AMBER) + pill(838, 151, 91, 'Sombre', VIOLET) + pill(939, 151, 82, 'OLED', BLUE)
    return out


def data_modes():
    out = surface(404, 33, 302, 194, 'SUR L’APPAREIL') + surface(743, 33, 303, 194, 'COMPTE CONNECTÉ')
    out += text(424, 101, 'Espace local', 23, MINT, 740) + text(425, 130, 'Sans compte Google', 16, MUTED)
    out += text(425, 175, 'Données dans un fichier privé', 14, INK)
    out += text(763, 101, 'Cloud Firestore', 23, VIOLET, 740) + text(764, 130, 'Firebase Auth · connexion Google', 16, MUTED)
    out += text(764, 175, 'Cache persistant de Firestore', 14, INK)
    out += arrow(712, 129, 736, 129, MINT)
    return out


hero()
architecture()
feature('01-accueil', '01', 'Accueil', ['Retrouver ses tâches et ses', 'repères du jour en un coup d’œil.'], 'TABLEAU DE BORD', overview(), MINT)
feature('02-planning', '02', 'Planning & tâches', ['Poser une date, une durée', 'et un contexte de travail.'], 'JOUR / SEMAINE / MOIS', planner(), BLUE)
feature('03-matrice', '03', 'Matrice Eisenhower', ['Distinguer ce qui est urgent', 'de ce qui est important.'], '4 QUADRANTS', matrix(), PINK)
feature('04-projets', '04', 'Projets & Gantt', ['Voir la durée des tâches et', 'la position des jalons.'], 'CHRONOLOGIE', gantt(), AMBER)
feature('05-focus', '05', 'Minuteur Focus', ['Travailler par session,', 'avec un fond sonore au choix.'], 'MINUTEUR / CHRONO / AUDIO', timer(), MINT)
feature('06-profils-focus', '06', 'Profils de Focus', ['Associer une tâche à un sujet', 'et retrouver le temps consacré.'], 'SUJET → TÂCHE → SESSION', focus_profiles())
feature('07-focus-analytics', '07', 'Focus Analytics', ['Comparer la durée des séances', 'par jour, mode et profil.'], 'COURBE / HEATMAP / RYTHME', focus_analytics(), MINT)
feature('08-progression', '08', 'Progression', ['Suivre le travail par matière', 'et relire ses résultats.'], 'OBJECTIFS / NOTES / BILAN', progress(), VIOLET)
feature('09-habitudes', '09', 'Habitudes & routines', ['Cocher un rituel par date', 'et suivre sa régularité.'], 'JOURNAL DE RÉGULARITÉ', habits(), MINT)
feature('10-echeances', '10', 'Échéances & jalons', ['Visualiser le temps restant', 'avant une date importante.'], 'COMPTE À REBOURS', countdown(), AMBER)
feature('11-recherche', '11', 'Recherche', ['Retrouver un élément sans', 'parcourir tous les écrans.'], '4 TYPES DE RÉSULTATS', search(), BLUE)
feature('12-plan-ia', '12', 'Plan d’étude IA', ['Transformer un objectif en', 'sessions datées à prévisualiser.'], 'FIREBASE AI LOGIC', pipeline([('Objectif', 'Date + budget'), ('Gemini', 'Proposition'), ('Aperçu', 'Relecture'), ('Planning', 'Appliquer')], VIOLET))
feature('13-assistant', '13', 'Assistant Light', ['Dialoguer avec l’agent et', 'garder la main sur ses actions.'], 'VALIDATION HUMAINE', agent(), PINK)
feature('14-documents', '14', 'Pièces jointes', ['Extraire le texte sur l’appareil', 'avant de décider de l’envoyer.'], 'EXTRACTION LOCALE', pipeline([('Sélection', 'PDF, PNG…'), ('Extraction', 'Texte / OCR'), ('Consentement', 'Avant envoi'), ('Fournisseur', 'Texte transmis')], BLUE), BLUE)
feature('15-personnalisation', '15', 'Personnalisation', ['Ajuster le profil, les couleurs', 'et les préférences de rappel.'], 'PROFIL / THÈMES / RÉGLAGES', appearance(), PINK)
feature('16-local-cloud', '16', 'Local et connecté', ['Commencer sans compte ;', 'se connecter si nécessaire.'], 'STOCKAGE / SYNCHRONISATION', data_modes(), MINT)
feature('livraison', 'CI', 'Chaîne de livraison', ['Valider le code, signer les', 'binaires, vérifier et publier.'], 'GITHUB ACTIONS', pipeline([('Validation', 'Build + tests'), ('Signature', 'APK + AAB'), ('Vérification', 'Signatures + SHA'), ('Publication', 'GitHub Release')], MINT), MINT)
