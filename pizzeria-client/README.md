# Thin client — Sistema di gestione pizzeria

Client a riga di comando per il progetto di Basi di Dati.
Adattamento del client di riferimento del corso, di cui conserva
architettura MVC, pattern DAO e gestione dei ruoli via utenti del DBMS.

## Stato

Completo: tutti e quattro i ruoli (cameriere, pizzaiolo, barman, gestore).

## Prerequisiti

1. MariaDB in esecuzione con lo schema creato da `pizzeria.sql`
2. JDK 17 o superiore

## Compilazione ed esecuzione

Dalla cartella del progetto:

    javac -d build -cp libs/mariadb-java-client-3.0.8.jar $(find src -name "*.java")
    java -cp build:libs/mariadb-java-client-3.0.8.jar it.uniroma2.dicii.bd.Main

Su Windows il separatore del classpath è `;` invece di `:`.

`resources/db.properties` è letto con percorso relativo, quindi il programma
va avviato con la cartella del progetto come directory di lavoro.

## Credenziali di prova

Nei dati di popolamento la password coincide con l'username:

| Ruolo | Username |
|---|---|
| Gestore | mrossi |
| Cameriere | lbianchi, gverdi, mferrari |
| Pizzaiolo | pesposito, aricci |
| Barman | fconti |

## Struttura

    src/it/uniroma2/dicii/bd/
      Main.java                 avvio
      controller/               orchestrazione: chiama i DAO, passa i dati alle view
      view/                     solo input e output su console
      model/dao/                un DAO per stored procedure
      model/domain/             oggetti restituiti dalle procedure
      model/dto/                record con i parametri di ingresso
      exception/                DAOException -> ControllerException -> ApplicationException

## Come aggiungere un ruolo

1. Un DTO in `model/dto` per ogni procedura con parametri
2. Una classe in `model/domain` per ogni procedura che restituisce righe
3. Un DAO in `model/dao` che implementa `GenericProcedureDAO<Input, Output>`
4. Una view con `showMenu()` e i metodi di stampa
5. Un controller che chiama `ConnectionFactory.changeRole(...)` e cicla sul menu
6. Il nuovo `case` nello `switch` di `ApplicationController`

## Nota sulla validazione

I controlli applicativi non sono replicati nel client: sono realizzati dalle
stored procedure, che segnalano gli errori con `SIGNAL SQLSTATE '45000'`.
I DAO li traducono in `DAOException` e le view ne mostrano il messaggio.
