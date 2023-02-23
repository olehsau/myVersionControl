## Great Versioning Tool (gvt)

Należy napisać system kontroli wersji plików - (bardzo) uproszczony GIT.

System obsługuje tylko pliki. Nie obsługuje podkatalogów, i nie musi (ale może) obsługiwać linków.

*UWAGA!* Do zakończenia działania programu prosze używać klasy `ExitHandler`, nie kończyć działania programu bezpośrendnim wywołaniem `System.exit`!

Podstawową jednostką działania są *wersje*. Każda *wersja* zawiera:
- numer wersji (od `0` do `Long.MAX_VALUE`);
- wiadomość (commit message), który został dodany przy zatwierdzaniu (`commit`) wersji;
- wszystkie pliki, które były dodane (komenda `add`) do gvt. Pliki zatwierdzone w konkretnej wersji nie mogą być w ramach tej wersji zmodyfikowane - zatwierdzenie ich modyfikacji oznacza utworzenie nowej wersji.
- *ostatnia wersja* to wersja, która została utworzona ostatnio. Tworzyć nową wersję mogą komendy: `init` (tylko 0), `add`, `detach`, `commit`. 

### Przykładowa struktura danych
```
.gvt
├── HEAD
└── versions
    ├── 0
    │   ├── COMMIT_MESSAGE
    │   └── files
    ├── 1
    │   ├── COMMIT_MESSAGE
    │   └── files
    │       └── file1.txt
    ├── 2
    │   ├── COMMIT_MESSAGE
    │   └── files
    │       ├── file1.txt
    │       └── file2.txt
    └── 3
        ├── COMMIT_MESSAGE
        └── files
            └── file2.txt
```
Plik `HEAD` przetrzymuje numer najnowszej wersji. Każda werjsa przechowuje w folderze `versions` kopię wszystkich kontrolowanych plików. Plik `COMMIT_MESSAGE` przechowuje bezpośrednio całą przekazaną wiadomość wersji (bez dodanego znaku nowej linii na końcu pliku).

### Uruchomienie aplikacji
- System powinien dostarczyć uruchamialną klasę `Gvt`. Klasa ta będzie używana do uruchomienia wszystkich komend.
- Komenda zawsze będzie pierwszym parametrem uruchomienia programu.
- W razie braku parametrów, program powinien wypisać na *System.out*: `Please specify command.`, oraz zwrócić kod błędu 1.
- W razie podania nieznanej komendy, program powinien wypisać na *System.out*: `Unknown command {specifed-command}.`, oraz zwrócić kod błędu 1.

### Zasady ogólne
- Wszystkie komendy (poza `init`) działają tylko w zainicjalizowanym katalogu. Jeżli bieżący katalog nie jest zainicjalizowany, wszystkie pozostałe komendy powinny wypisać na *System.out* komunikat: `Current directory is not initialized. Please use "init" command to initialize.`, oraz zwrócić kod błędu -2.
- Jeśli nie podano inaczej, to w razie wystąpienia błędu systemu operacyjnego (np. brak miejsca na dysku, brak praw do zapisu) należy wypisać na *System.out*: `Underlying system problem. See ERR for details.`, zwrócić kod błędu -3, oraz wypisać na *System.err* stack trace.

### Zasady dla komend: add, detach, commit
- posiadają opcjonalny parametr, `-m {"Treść wiadomości w cudzysłowiu}"`, który może byc podany jako ostatni parametr. Jest to *wiadomość użytkownika*. Należy ją dokleić do *domyślnej wiadmości*; razem utworzą wiadomość wersji.

### Komendy
#### init
Inicjalizuje system gvt w bieżącym katalogu, oraz ustawia aktywną i ostanią wersję na 0. Wiadomość do wersji 0: `GVT initialized.`

- Jeżeli katalog już był zainicjowany, należy wypisać na *System.out*: `Current directory is already initialized.`, oraz zwrócić kod błędu 10.
- Jeżeli inicjalizaja się udała, należy wypisać na *System.out*: `Current directory initialized successfully.`.

Przy inicjacji, Gvt powinien utworzyć katalog o nazwie `.gvt`. Wewnątrz tego katalogu system może przechowywać wszystkie dane niezbędne do jego działania.

#### add
Dodaje plik wskazany jako parametry do tej komendy. Przyjmuje też opcjonalną wiadmość użytkownika (opisane w zasadach ogólnych).

Jeśli nie wskazano pliku, należy wypisac na *System.out*: `Please specify file to add.`, oraz zwrócić kod błędu 20.

Jeśli wskazano plik, to należy:
- w razie powodzenia wypisać na *System.out*: `File added successfully. File: {file-name}` oraz utworzyć nową wersję.
- jeśli plik nie istnieje wypisać na *System.out*: `File not found. File: {file-name}`, oraz zwrócić kod błędu 21.
- jeśli plik jest już dodany, należy wypisać na *System.out*: `File already added. File: {file-name}`
- jeśli wystąpił jakikolwiek inny błąd, należy wypisać *System.out*: `File cannot be added. See ERR for details. File: {file-name}`, wypisać na *System.err* stack trace, oraz zwrócić kod błędu 22.

Domyslna wiadomość: `Added file: {file-name}`. 

#### detach
Odłącza od gvt (ale nie usuwa z systemu plików!) plik wskazany jako parametr do komendy. Przyjmuje też opcjonalną wiadmość użytkownika (opisane w zasadach ogólnych).

Jeśli nie wskazano pliku, należy wypisać na *System.out*: `Please specify file to detach.`, oraz zwrócić kod błędu 30.

Jeśli wskazano plik, to należy:
- w razie powodzenia wypisać na *System.out*: `File detached successfully. File: {file-name}`, oraz utworzyć nową wersję.
- jeśli plik nie jest dodany do gvt, należy wypisac na *System.out*: `File is not added to gvt. File: {file-name}`
- jeśli wystąpił jakikolwiek inny błąd, należy wypisać *System.out*: `File cannot be detached, see ERR for details. File: {file-name}`, wypisać na *System.err* stack trace, oraz zwrócić kod błędu 31.

Domyslna wiadomość: `File detached successfully. File: {file-name}`. 

#### checkout
Przywraca pliki do stanu z konkretnej, wskazanej w parametrze, wersji.

Komenda nie zmienia stanu kontrolowania plików przez GVT. Np: jeśli plik był kontrolowany w przywracanej wersji, a nie jest kontrolowany w ostatniej wersji, to NIE nalezy go dodawać do GVT, tylko przywrócić jego zawartość (lub odtworzyć, jeśli był w międzyczasie usunięty). Pliki, które w obu wersjach nie są kontrolowane, pozostają niezmienione.

Przyjmuje 1 parametr: numer wersji do przywrócenia.

- jeśli podana wersja jest nieprawidłowa (nie istnieje, albo nie jest to liczba) należy wypisać na *System.out*: `Invalid version number: {specified-number}.`, oraz zwrócić kod błędu 40.
- jeśli podana wersja jest prawidłowa, należy przywrócić stan wszystkich plików do stanu z podanej wersji, oraz wypisać na *System.out*: `Version {n} checked out successfully.`
- w razie innych błędów, należy zastosować zasady ogólne.

#### commit
Tworzy nową wersję w GVT z plikem podanym jako parametr. Przyjmuje też opcjonalną wiadmość użytkownika (opisane w zasadach ogólnych).

Jeśli nie wskazano pliku, należy wypisać na *System.out*: `Please specify file to commit.`, oraz zwrócić kod błędu 50.

Jeżeli wskazano plik, to należy:
- jeśli plik był dodany i nadal istnieje, należy utworzyć nową wersję, oraz wypisać na *System.out*: `File committed successfully. File: {file-name}`.
- jeśli plik nie był dodany, należy wypisać na *System.out*: `File is not added to gvt. File: {file-name}`
- jeśli plik nie istnieje, należy wypisać na *System.out*: `File not found. File: {file-name}`, oraz zwrócić kod błędu 51.
- jeśli wystąpił jakikolwiek inny błąd, należy wypisać *System.out*: `File cannot be committed, see ERR for details. File: {file-name}`, wypisać na *System.err* stack trace, oraz zwrócić kod 52.

Domyślna wiadomość: `Committed file: {file-name}`.

#### history
Wyświetla historię wersji. 

Format:`{numer-wersji}: {commit message}`. Każda wersja jest wyświetlana w nowej linii. Jeśli wiadomość (commit message) jest wieloliniowa, należy wyświetlić tylko pierwszą linię.

- jeśli nie wyspecyfikowano parametrów, to wyświetlane są wszystkie wersje.
- paramter `-last {n}`: wyświetla ostatnie n wersji.
- błędne parametry są ignorowane, i traktowane jako brak parametrów.

#### version
Wyświetla detalie wersji, o numerze podanym jako paramter.

- jeśli nie podano parametru, wyświetla ostatnią wersję.
- jeśli podana wersja jest nieprawidłowa (nie istnieje, albo nie jest to liczba) należy wypisać na *System.out*: `Invalid version number: {specified-number}.`, oraz zwrócić kod błędu 60.

Format: 
```
Version: {numer-wersji}
{commit message}
```
