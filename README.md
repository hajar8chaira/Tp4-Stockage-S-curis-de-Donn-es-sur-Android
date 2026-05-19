# TP 4 : Implémentation d'une Solution Globale de Stockage Sécurisé sur Android

Ce document présente l'implémentation de la solution de stockage sécurisé de données sur la plateforme Android (TP 4). L'application applique des concepts cryptographiques avancés et respecte les directives de sécurité de l'OWASP Mobile (M1: Improper Data Storage, M2: Insecure Cryptography).

---

## 1. Objectifs du Travail Pratique
L'objectif est de concevoir et réaliser une application Android dotée d'une architecture moderne avec une interface utilisateur unifiée, stylisée en tons rose pastel et rose vif, orchestrant cinq modules fondamentaux de protection des données :
* **Authentification Robuste** : Gestion des sessions utilisateurs avec dérivation de clé PBKDF2 et sel cryptographique.
* **Stockage Interne Chiffré** : Utilisation d'algorithmes symétriques pour le chiffrement des fichiers locaux de l'application.
* **Stockage Externe Sécurisé** : Gestion des fichiers dans le stockage externe dédié en conformité avec Scoped Storage.
* **Base de données Chiffrée** : Intégration de Room et SQLCipher pour le chiffrement transparent de la base de données.
* **Diagnostic et Remédiation** : Analyseur de vulnérabilités locales avec console interactive de remédiation de code.

---

## 2. Étape 1 : Authentification et Dérivation de Clés (PBKDF2)
Pour éviter de stocker les mots de passe en clair ou d'utiliser des algorithmes obsolètes comme MD5 ou SHA-1, l'application utilise **PBKDF2WithHmacSHA256** pour dériver les clés d'accès.

### Spécifications Techniques
* **Algorithme** : PBKDF2 avec HMAC-SHA256.
* **Paramètres** : 10 000 itérations, longueur de clé de 256 bits.
* **Salage** : Sel aléatoire de 16 octets généré par `SecureRandom` pour chaque utilisateur lors de l'inscription, protégeant contre les attaques par table de pré-calcul (rainbow tables).

### Captures d'Écran Associées
Le cycle d'authentification (connexion et inscription) est illustré par les captures suivantes :

* **Interface de Connexion principale** :
  ![Connexion](./img-tp4-devsec/1.png)

* **Formulaire d'Inscription d'un nouvel utilisateur** :
  ![Inscription](./img-tp4-devsec/2.png)

* **Validation de la session après connexion** :
  ![Dashboard](./img-tp4-devsec/3.png)

---

## 3. Étape 2 : Stockage Interne avec Chiffrement Symétrique (AES-256)
Le stockage interne est chiffré au niveau applicatif à l'aide d'une clé AES-256 générée dynamiquement et stockée de manière sécurisée dans le Keystore Android.

### Spécifications Techniques
* **Algorithme de chiffrement** : AES/GCM/NoPadding (chiffrement authentifié garantissant l'intégrité et la confidentialité des fichiers).
* **Gestion des clés** : Clé maîtresse stockée dans le système d'authentification matériel du Keystore Android (TEE/StrongBox).
* **Contrôle d'accès** : Fichiers stockés dans le répertoire privé (`context.getFilesDir()`), inaccessibles aux autres applications du système.

### Captures d'Écran Associées
L'interface de gestion du stockage interne privé permet de sauvegarder, charger et lister les informations chiffrées :

* **Sauvegarde et chargement de données privées** :
  ![Stockage Interne](./img-tp4-devsec/4-charger.png)

* **Affichage de la liste des fichiers chiffrés en interne** :
  ![Liste Interne](./img-tp4-devsec/5-lister.png)

---

## 4. Étape 3 : Stockage Externe et Scoped Storage
Sur les versions modernes d'Android, l'accès au stockage externe partagé est restreint pour préserver la vie privée des utilisateurs. L'application implémente le modèle **Scoped Storage**.

### Spécifications Techniques
* **Emplacement** : Utilisation du répertoire externe spécifique à l'application (`context.getExternalFilesDir()`).
* **Avantage de sécurité** : Ne requiert aucune permission globale en écriture/lecture (comme `WRITE_EXTERNAL_STORAGE`), limitant ainsi le privilège accordé à l'application.
* **Comportement système** : Les fichiers stockés dans cet espace sont automatiquement supprimés lors de la désinstallation de l'application.

### Captures d'Écran Associées
Le module gère de façon transparente la persistance sécurisée sur le support externe :

* **Création d'un fichier externe applicatif** :
  ![Stockage Externe](./img-tp4-devsec/6-externe.png)

* **Lecture sécurisée du stockage externe** :
  ![Lecture Externe](./img-tp4-devsec/7-charger.png)

* **Visualisation de la liste des ressources externes** :
  ![Liste Externe](./img-tp4-devsec/8-lister.png)

---

## 5. Étape 4 : Base de Données Room Chiffrée (SQLCipher)
La persistance structurée de l'application repose sur le framework ORM **Room**, sécurisé par la bibliothèque de chiffrement de base de données **SQLCipher**.

### Spécifications Techniques
* **Moteur de chiffrement** : SQLCipher (chiffrement complet des pages SQLite).
* **Algorithme** : AES-256 en mode CBC avec dérivation de clé PBKDF2.
* **Intégration** : Initialisation via le chargement dynamique des bibliothèques natives (`SQLiteDatabase.loadLibs(context)`) et l'injection d'un support de base de données d'usine (`SupportFactory`) configuré avec une phrase de passe cryptographique.

### Captures d'Écran Associées
L'application implémente un système de prise de notes sécurisée où chaque note est chiffrée individuellement en base de données :

* **Interface de gestion de la Base de Données (Room + SQLCipher)** :
  ![Base de données](./img-tp4-devsec/9-bdd.png)

* **Formulaire d'ajout d'une nouvelle note chiffrée** :
  ![Ajout de Note](./img-tp4-devsec/10-%20notes.png)

* **Validation de la saisie d'informations** :
  ![Validation de saisie](./img-tp4-devsec/11.png)

* **Chiffrement et confirmation de la persistance** :
  ![Confirmation](./img-tp4-devsec/12.png)

* **Visualisation de la liste des notes déchiffrées en temps réel** :
  ![Affichage des notes](./img-tp4-devsec/13-chiffrer-lister.png)

* **Mécanisme de décryptage dynamique à la demande** :
  ![Decrypt Note](./img-tp4-devsec/14-dechiffrer.png)

---

## 6. Étape 5 : Analyseur de Sécurité et Console Interactive de Remédiation
Pour valider l'état de sécurité global du terminal, le dernier module est un tableau de bord de diagnostic de sécurité dynamique qui analyse l'environnement en temps réel.

### Vulnérabilités Analysées
* **Sauvegardes de Données (`allowBackup`)** : Vérifie si la configuration autorise la sauvegarde ADB des données privées (Risque élevé de fuite).
* **Disponibilité de la StrongBox** : Détermine si l'appareil possède une puce physique de protection des clés (HSM).
* **Mode Debugging (`debuggable`)** : Détecte si l'application est compilée avec les drapeaux de débogage actifs.
* **Environnement d'Exécution (Émulateur)** : Analyse le matériel pour interdire l'exécution dans des environnements de virtualisation non contrôlés.

### Interactivité et Console de Code
Chaque anomalie détectée est cliquable et ouvre une boîte de dialogue fournissant :
* Une analyse de l'impact de la vulnérabilité.
* La recommandation théorique associée.
* Une console de code affichant le correctif Java/XML/Gradle exact.
* Un bouton interactif de **Simulation de correction** permettant de corriger dynamiquement le rapport pour des fins de démonstration académique.

### Captures d'Écran Associées
Le fonctionnement du module de diagnostic est détaillé ci-dessous :

* **Rapport d'audit de sécurité initial** :
  ![Tableau Sécurité](./img-tp4-devsec/15-securite.png)

* **Détail d'une vulnérabilité HIGH cliquée (Mode Debug actif)** :
  ![Vulnerability Detail High](./img-tp4-devsec/16-hight.png)

* **Détail d'une vulnérabilité LOW cliquée (Détection d'émulateur)** :
  ![Vulnerability Detail Low](./img-tp4-devsec/17.png)

---

## 7. Synthèse des Mécanismes de Protection

| Type de Donnée | Emplacement Physique | Algorithme / Technologie | Statut de Protection |
| :--- | :--- | :--- | :--- |
| Mots de passe | Base de données Room | PBKDF2WithHmacSHA256 | Haché avec sel unique |
| Fichiers internes | `/data/data/.../files/` | AES-256-GCM | Chiffrement symétrique |
| Fichiers externes | `/sdcard/Android/data/...` | Scoped Storage | Isolement sandbox |
| Données structurées | Base SQLite Room | SQLCipher (AES-256-CBC) | Base entièrement chiffrée |
| Clés cryptographiques | Keystore Android | Keystore Hardware (TEE) | Stockage inviolable |

---

## 8. Démonstration Vidéo de l'Application
Une vidéo de démonstration illustrant le scénario complet d'exécution de l'application (inscription, authentification, opérations sur fichiers internes/externes, notes chiffrées dans Room+SQLCipher, et analyse de vulnérabilités cliquable avec remédiation simulée) est disponible :
* **Vidéo de démonstration locale** : [demo.mp4](./demo.mp4) (à lire localement ou à télécharger à partir du dépôt).
