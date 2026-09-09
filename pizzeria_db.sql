-- =====================================================================
-- Basi di Dati - Progetto A.A. 2025/2026
-- Sistema di gestione di una pizzeria
--
-- DBMS di riferimento: MariaDB 10.6 o superiore
-- Esecuzione:  mysql -u root -p < pizzeria.sql
--
-- Il file crea lo schema completo: tabelle e vincoli, indici, viste,
-- trigger, evento di manutenzione, stored procedure, utenti applicativi
-- con i relativi privilegi e un insieme minimo di dati di prova.
-- =====================================================================


-- =====================================================================
-- 0. CREAZIONE DELLO SCHEMA
-- =====================================================================

DROP DATABASE IF EXISTS pizzeria;
CREATE DATABASE pizzeria
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE pizzeria;


-- =====================================================================
-- 1. TABELLE E VINCOLI
-- =====================================================================

DROP TABLE IF EXISTS Tavolo;
CREATE TABLE Tavolo (
    numero  TINYINT UNSIGNED               NOT NULL,
    posti   TINYINT UNSIGNED               NOT NULL,
    stato   ENUM('LIBERO','OCCUPATO')      NOT NULL DEFAULT 'LIBERO',

    CONSTRAINT pk_tavolo  PRIMARY KEY (numero),
    CONSTRAINT chk_posti  CHECK (posti > 0)
) ENGINE=InnoDB;


DROP TABLE IF EXISTS Cliente;
CREATE TABLE Cliente (
    telefono VARCHAR(20) NOT NULL,
    nome     VARCHAR(60) NOT NULL,

    CONSTRAINT pk_cliente PRIMARY KEY (telefono)
) ENGINE=InnoDB;


DROP TABLE IF EXISTS Dipendente;
CREATE TABLE Dipendente (
    username VARCHAR(32) NOT NULL,
    password CHAR(64)    NOT NULL,          -- digest SHA-256
    nome     VARCHAR(40) NOT NULL,
    cognome  VARCHAR(40) NOT NULL,
    ruolo    ENUM('CAMERIERE','PIZZAIOLO','BARMAN','GESTORE') NOT NULL,

    CONSTRAINT pk_dipendente PRIMARY KEY (username)
) ENGINE=InnoDB;


DROP TABLE IF EXISTS Prodotto;
CREATE TABLE Prodotto (
    codice      SMALLINT UNSIGNED       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(60)             NOT NULL,
    prezzo      DECIMAL(5,2)            NOT NULL,
    tipo        ENUM('PIZZA','BEVANDA') NOT NULL,
    disponibile BOOLEAN                 NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_prodotto      PRIMARY KEY (codice),
    CONSTRAINT uq_prodotto_nome UNIQUE (nome),
    CONSTRAINT chk_prezzo       CHECK (prezzo > 0)
) ENGINE=InnoDB;


DROP TABLE IF EXISTS Comanda;
CREATE TABLE Comanda (
    ID                 INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    tipo               ENUM('LOCALE','ASPORTO','CONSEGNA')  NOT NULL,
    dataApertura       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dataChiusura       DATETIME         NULL     DEFAULT NULL,
    stato              ENUM('APERTA','COMPLETATA')          NOT NULL DEFAULT 'APERTA',
    totale             DECIMAL(7,2)     NULL     DEFAULT NULL,
    numeroTavolo       TINYINT UNSIGNED NULL     DEFAULT NULL,
    telefonoCliente    VARCHAR(20)      NULL     DEFAULT NULL,
    indirizzo          VARCHAR(120)     NULL     DEFAULT NULL,
    usernameDipendente VARCHAR(32)      NOT NULL,

    CONSTRAINT pk_comanda PRIMARY KEY (ID),

    CONSTRAINT fk_comanda_tavolo
        FOREIGN KEY (numeroTavolo)       REFERENCES Tavolo(numero),
    CONSTRAINT fk_comanda_cliente
        FOREIGN KEY (telefonoCliente)    REFERENCES Cliente(telefono),
    CONSTRAINT fk_comanda_dipendente
        FOREIGN KEY (usernameDipendente) REFERENCES Dipendente(username),

    -- Coerenza tra modalita della comanda e attributi opzionali:
    -- sostituisce il vincolo che nello schema concettuale era garantito
    -- dalla generalizzazione, accorpata in fase di ristrutturazione.
    CONSTRAINT chk_modalita CHECK (
        (tipo = 'LOCALE'   AND numeroTavolo IS NOT NULL
                           AND telefonoCliente IS NULL     AND indirizzo IS NULL)
     OR (tipo = 'ASPORTO'  AND numeroTavolo IS NULL
                           AND telefonoCliente IS NULL     AND indirizzo IS NULL)
     OR (tipo = 'CONSEGNA' AND numeroTavolo IS NULL
                           AND telefonoCliente IS NOT NULL AND indirizzo IS NOT NULL)
    ),

    -- Coerenza tra stato della comanda e dati di chiusura.
    CONSTRAINT chk_chiusura CHECK (
        (stato = 'APERTA'     AND dataChiusura IS NULL     AND totale IS NULL)
     OR (stato = 'COMPLETATA' AND dataChiusura IS NOT NULL AND totale IS NOT NULL)
    )
) ENGINE=InnoDB;


DROP TABLE IF EXISTS ElementoComanda;
CREATE TABLE ElementoComanda (
    IDComanda           INT UNSIGNED      NOT NULL,
    numeroProgressivo   SMALLINT UNSIGNED NOT NULL,
    codiceProdotto      SMALLINT UNSIGNED NOT NULL,
    stato               ENUM('IN_ATTESA','PRONTO','CONSEGNATO') NOT NULL DEFAULT 'IN_ATTESA',
    prezzoUnitario      DECIMAL(5,2)      NOT NULL,
    usernamePreparatore VARCHAR(32)       NULL DEFAULT NULL,

    -- Chiave primaria composta: traduzione dell'identificatore esterno.
    CONSTRAINT pk_elemento PRIMARY KEY (IDComanda, numeroProgressivo),

    -- Dipendenza esistenziale dell'elemento dalla comanda.
    CONSTRAINT fk_elemento_comanda
        FOREIGN KEY (IDComanda)           REFERENCES Comanda(ID) ON DELETE CASCADE,
    CONSTRAINT fk_elemento_prodotto
        FOREIGN KEY (codiceProdotto)      REFERENCES Prodotto(codice),
    CONSTRAINT fk_elemento_preparatore
        FOREIGN KEY (usernamePreparatore) REFERENCES Dipendente(username)
) ENGINE=InnoDB;


-- =====================================================================
-- 2. INDICI
--    Non si riportano gli indici autogenerati per chiavi primarie ed
--    esterne, ma soltanto quelli introdotti per motivi prestazionali.
-- =====================================================================

-- Serve alle consultazioni di sala e cucina (C3, P1, B1, G4).
CREATE INDEX idx_elemento_stato
    ON ElementoComanda (stato);

-- Serve alle operazioni statistiche (G7, G8).
CREATE INDEX idx_comanda_stato_chiusura
    ON Comanda (stato, dataChiusura);


-- =====================================================================
-- 3. VISTE
-- =====================================================================

-- Coda di preparazione, usata da pizzaiolo e barman.
DROP VIEW IF EXISTS v_coda_preparazione;
CREATE VIEW v_coda_preparazione AS
SELECT e.IDComanda,
       e.numeroProgressivo,
       p.nome   AS prodotto,
       p.tipo   AS tipo,
       c.tipo   AS modalita,
       c.numeroTavolo,
       c.dataApertura
FROM ElementoComanda e
JOIN Prodotto p ON e.codiceProdotto = p.codice
JOIN Comanda  c ON e.IDComanda      = c.ID
WHERE e.stato = 'IN_ATTESA';


-- Prodotti pronti, usata dai camerieri e dal gestore.
DROP VIEW IF EXISTS v_elementi_pronti;
CREATE VIEW v_elementi_pronti AS
SELECT e.IDComanda,
       e.numeroProgressivo,
       p.nome AS prodotto,
       c.tipo AS modalita,
       c.numeroTavolo
FROM ElementoComanda e
JOIN Prodotto p ON e.codiceProdotto = p.codice
JOIN Comanda  c ON e.IDComanda      = c.ID
WHERE e.stato = 'PRONTO';


-- Base per le statistiche. Deliberatamente priva di GROUP BY: una vista
-- aggregata verrebbe materializzata e il filtro sul periodo agirebbe solo
-- dopo l'aggregazione, vanificando l'indice idx_comanda_stato_chiusura.
DROP VIEW IF EXISTS v_comande_completate;
CREATE VIEW v_comande_completate AS
SELECT ID, dataChiusura, totale
FROM Comanda
WHERE stato = 'COMPLETATA';


-- =====================================================================
-- 4. TRIGGER
--    Limitati alle asserzioni che coinvolgono piu tabelle e devono valere
--    indipendentemente dalla procedura che effettua la scrittura.
-- =====================================================================

DELIMITER $$

DROP TRIGGER IF EXISTS trg_elemento_no_update$$
CREATE TRIGGER trg_elemento_no_update
BEFORE UPDATE ON ElementoComanda
FOR EACH ROW
BEGIN
    IF (SELECT stato FROM Comanda WHERE ID = OLD.IDComanda) = 'COMPLETATA' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Impossibile modificare elementi di una comanda completata';
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_comanda_no_update$$
CREATE TRIGGER trg_comanda_no_update
BEFORE UPDATE ON Comanda
FOR EACH ROW
BEGIN
    IF OLD.stato = 'COMPLETATA' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Impossibile modificare una comanda completata';
    END IF;
END$$

DELIMITER ;


-- =====================================================================
-- 5. EVENTO DI MANUTENZIONE
--    Realizza la politica di conservazione quinquennale assunta nel
--    calcolo dei volumi. La cancellazione degli elementi avviene per
--    effetto della cascata sulla chiave esterna.
-- =====================================================================

SET GLOBAL event_scheduler = ON;

DELIMITER $$

DROP EVENT IF EXISTS ev_pulizia_storico;
CREATE EVENT ev_pulizia_storico
ON SCHEDULE EVERY 1 MONTH
DO
BEGIN
    DELETE FROM Comanda
    WHERE stato = 'COMPLETATA'
      AND dataChiusura < CURDATE() - INTERVAL 5 YEAR;
END$$

DELIMITER ;


-- =====================================================================
-- 6. STORED PROCEDURES
-- =====================================================================

DELIMITER $$

-- ---------------------------------------------------------------------
-- L1 - Autenticazione
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_login$$
CREATE PROCEDURE sp_login(
    IN  p_username VARCHAR(32),
    IN  p_password VARCHAR(64),
    OUT p_ruolo    VARCHAR(16)
)
BEGIN
    SET p_ruolo = NULL;

    SELECT ruolo INTO p_ruolo
    FROM Dipendente
    WHERE username = p_username
      AND password = SHA2(p_password, 256);

    IF p_ruolo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Credenziali non valide';
    END IF;
END$$


-- ---------------------------------------------------------------------
-- C1 - Assegna un tavolo libero e apre la comanda locale.
-- Transazionale: due scritture e verifica di disponibilita protetta da
-- lock esclusivo, per impedire che due camerieri assegnino lo stesso
-- tavolo contemporaneamente.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_apri_comanda_locale$$
CREATE PROCEDURE sp_apri_comanda_locale(
    IN  p_numeroTavolo TINYINT UNSIGNED,
    IN  p_username     VARCHAR(32),
    OUT p_idComanda    INT UNSIGNED
)
BEGIN
    DECLARE v_stato ENUM('LIBERO','OCCUPATO') DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    SELECT stato INTO v_stato
    FROM Tavolo WHERE numero = p_numeroTavolo FOR UPDATE;

    IF v_stato IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tavolo inesistente';
    END IF;
    IF v_stato = 'OCCUPATO' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tavolo gia occupato';
    END IF;

    INSERT INTO Comanda (tipo, numeroTavolo, usernameDipendente)
    VALUES ('LOCALE', p_numeroTavolo, p_username);
    SET p_idComanda = LAST_INSERT_ID();

    UPDATE Tavolo SET stato = 'OCCUPATO' WHERE numero = p_numeroTavolo;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- C2, G3 - Aggiunge un'unita di prodotto a una comanda aperta.
-- Il lock sulla comanda serializza il calcolo del numero progressivo.
-- Il prezzo viene congelato sull'elemento al momento dell'inserimento.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_aggiungi_elemento$$
CREATE PROCEDURE sp_aggiungi_elemento(
    IN  p_idComanda      INT UNSIGNED,
    IN  p_codiceProdotto SMALLINT UNSIGNED,
    OUT p_numero         SMALLINT UNSIGNED
)
BEGIN
    DECLARE v_statoComanda ENUM('APERTA','COMPLETATA') DEFAULT NULL;
    DECLARE v_disponibile  BOOLEAN      DEFAULT NULL;
    DECLARE v_prezzo       DECIMAL(5,2) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    SELECT stato INTO v_statoComanda
    FROM Comanda WHERE ID = p_idComanda FOR UPDATE;

    IF v_statoComanda IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Comanda inesistente';
    END IF;
    IF v_statoComanda = 'COMPLETATA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Comanda gia completata';
    END IF;

    SELECT disponibile, prezzo INTO v_disponibile, v_prezzo
    FROM Prodotto WHERE codice = p_codiceProdotto;

    IF v_disponibile IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Prodotto inesistente';
    END IF;
    IF NOT v_disponibile THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Prodotto non disponibile';
    END IF;

    SELECT COALESCE(MAX(numeroProgressivo), 0) + 1 INTO p_numero
    FROM ElementoComanda WHERE IDComanda = p_idComanda;

    INSERT INTO ElementoComanda
        (IDComanda, numeroProgressivo, codiceProdotto, prezzoUnitario)
    VALUES (p_idComanda, p_numero, p_codiceProdotto, v_prezzo);

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- C3 - Prodotti pronti da servire ai tavoli.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_elementi_pronti_sala$$
CREATE PROCEDURE sp_elementi_pronti_sala()
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT IDComanda, numeroProgressivo, prodotto, numeroTavolo
    FROM v_elementi_pronti
    WHERE modalita = 'LOCALE'
    ORDER BY numeroTavolo;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- C4 - Registra la consegna al tavolo. La verifica dello stato e
-- l'aggiornamento sono in un'unica istruzione condizionale, cosi che due
-- camerieri non possano registrare due volte la stessa consegna.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_segna_elemento_consegnato$$
CREATE PROCEDURE sp_segna_elemento_consegnato(
    IN p_idComanda INT UNSIGNED,
    IN p_numero    SMALLINT UNSIGNED
)
BEGIN
    UPDATE ElementoComanda
    SET stato = 'CONSEGNATO'
    WHERE IDComanda         = p_idComanda
      AND numeroProgressivo = p_numero
      AND stato             = 'PRONTO';

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Elemento inesistente o non ancora pronto';
    END IF;
END$$


-- ---------------------------------------------------------------------
-- P1, B1 - Coda di preparazione del tipo richiesto, in ordine di arrivo.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_coda_preparazione$$
CREATE PROCEDURE sp_coda_preparazione(
    IN p_tipo ENUM('PIZZA','BEVANDA')
)
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT IDComanda, numeroProgressivo, prodotto, modalita, numeroTavolo
    FROM v_coda_preparazione
    WHERE tipo = p_tipo
    ORDER BY dataApertura, IDComanda, numeroProgressivo;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- P2, B2 - Registra la preparazione di un elemento.
-- Realizza la regola aziendale che riserva le pizze al pizzaiolo e le
-- bevande al barman: il controllo non e delegabile ai privilegi, poiche
-- entrambi i ruoli eseguono questa stessa procedura.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_segna_elemento_preparato$$
CREATE PROCEDURE sp_segna_elemento_preparato(
    IN p_idComanda INT UNSIGNED,
    IN p_numero    SMALLINT UNSIGNED,
    IN p_username  VARCHAR(32)
)
BEGIN
    DECLARE v_tipo  ENUM('PIZZA','BEVANDA') DEFAULT NULL;
    DECLARE v_stato ENUM('IN_ATTESA','PRONTO','CONSEGNATO') DEFAULT NULL;
    DECLARE v_ruolo ENUM('CAMERIERE','PIZZAIOLO','BARMAN','GESTORE') DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    SELECT e.stato, p.tipo INTO v_stato, v_tipo
    FROM ElementoComanda e
    JOIN Prodotto p ON e.codiceProdotto = p.codice
    WHERE e.IDComanda         = p_idComanda
      AND e.numeroProgressivo = p_numero
    FOR UPDATE;

    IF v_stato IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Elemento inesistente';
    END IF;
    IF v_stato <> 'IN_ATTESA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Elemento gia preparato';
    END IF;

    SELECT ruolo INTO v_ruolo FROM Dipendente WHERE username = p_username;

    -- Necessario prima del confronto sul ruolo: con v_ruolo nullo le
    -- disuguaglianze seguenti varrebbero NULL, la condizione risulterebbe
    -- falsa e il controllo verrebbe eluso.
    IF v_ruolo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dipendente inesistente';
    END IF;

    IF (v_tipo = 'PIZZA'   AND v_ruolo <> 'PIZZAIOLO')
    OR (v_tipo = 'BEVANDA' AND v_ruolo <> 'BARMAN') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il ruolo non e abilitato a preparare questo prodotto';
    END IF;

    UPDATE ElementoComanda
    SET stato = 'PRONTO', usernamePreparatore = p_username
    WHERE IDComanda = p_idComanda AND numeroProgressivo = p_numero;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G1 - Apre una comanda da asporto.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_apri_comanda_asporto$$
CREATE PROCEDURE sp_apri_comanda_asporto(
    IN  p_username  VARCHAR(32),
    OUT p_idComanda INT UNSIGNED
)
BEGIN
    INSERT INTO Comanda (tipo, usernameDipendente)
    VALUES ('ASPORTO', p_username);
    SET p_idComanda = LAST_INSERT_ID();
END$$


-- ---------------------------------------------------------------------
-- G2 - Registra una comanda telefonica in consegna.
-- Transazionale: puo inserire il cliente se non gia presente in
-- anagrafica, oltre alla comanda.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_apri_comanda_consegna$$
CREATE PROCEDURE sp_apri_comanda_consegna(
    IN  p_telefono  VARCHAR(20),
    IN  p_nome      VARCHAR(60),
    IN  p_indirizzo VARCHAR(120),
    IN  p_username  VARCHAR(32),
    OUT p_idComanda INT UNSIGNED
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM Cliente WHERE telefono = p_telefono) THEN
        INSERT INTO Cliente (telefono, nome) VALUES (p_telefono, p_nome);
    END IF;

    INSERT INTO Comanda (tipo, telefonoCliente, indirizzo, usernameDipendente)
    VALUES ('CONSEGNA', p_telefono, p_indirizzo, p_username);
    SET p_idComanda = LAST_INSERT_ID();

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G4 - Prodotti pronti da ritirare per asporto e consegne.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_elementi_pronti_ritiro$$
CREATE PROCEDURE sp_elementi_pronti_ritiro()
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT IDComanda, numeroProgressivo, prodotto, modalita
    FROM v_elementi_pronti
    WHERE modalita IN ('ASPORTO','CONSEGNA')
    ORDER BY IDComanda;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G5 - Chiude la comanda. Verifica che tutti gli elementi siano stati
-- consegnati, calcola e memorizza il totale, libera il tavolo se la
-- comanda era consumata sul posto. Le tre scritture devono essere
-- atomiche: un'interruzione lascerebbe il tavolo inutilizzabile.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_completa_comanda$$
CREATE PROCEDURE sp_completa_comanda(
    IN p_idComanda INT UNSIGNED
)
BEGIN
    DECLARE v_stato    ENUM('APERTA','COMPLETATA') DEFAULT NULL;
    DECLARE v_tavolo   TINYINT UNSIGNED DEFAULT NULL;
    DECLARE v_pendenti INT          DEFAULT 0;
    DECLARE v_totale   DECIMAL(7,2) DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    SELECT stato, numeroTavolo INTO v_stato, v_tavolo
    FROM Comanda WHERE ID = p_idComanda FOR UPDATE;

    IF v_stato IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Comanda inesistente';
    END IF;
    IF v_stato = 'COMPLETATA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Comanda gia completata';
    END IF;

    SELECT COUNT(*) INTO v_pendenti
    FROM ElementoComanda
    WHERE IDComanda = p_idComanda AND stato <> 'CONSEGNATO';

    IF v_pendenti > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esistono prodotti non ancora consegnati';
    END IF;

    SELECT COALESCE(SUM(prezzoUnitario), 0) INTO v_totale
    FROM ElementoComanda WHERE IDComanda = p_idComanda;

    UPDATE Comanda
    SET stato = 'COMPLETATA', dataChiusura = NOW(), totale = v_totale
    WHERE ID = p_idComanda;

    IF v_tavolo IS NOT NULL THEN
        UPDATE Tavolo SET stato = 'LIBERO' WHERE numero = v_tavolo;
    END IF;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G6 - Scontrino non fiscale. I prezzi riportati sono quelli congelati
-- sugli elementi, non quelli correnti del menu.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_stampa_scontrino$$
CREATE PROCEDURE sp_stampa_scontrino(
    IN p_idComanda INT UNSIGNED
)
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT c.ID, c.tipo, c.dataChiusura, c.totale,
           e.numeroProgressivo, p.nome AS prodotto, e.prezzoUnitario
    FROM Comanda c
    JOIN ElementoComanda e ON e.IDComanda      = c.ID
    JOIN Prodotto p        ON e.codiceProdotto = p.codice
    WHERE c.ID = p_idComanda AND c.stato = 'COMPLETATA'
    ORDER BY e.numeroProgressivo;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G7 - Entrate giornaliere. Il filtro e espresso come intervallo
-- semiaperto per consentire l'uso dell'indice composto.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_entrate_giornaliere$$
CREATE PROCEDURE sp_entrate_giornaliere(
    IN p_giorno DATE
)
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT COUNT(*)                 AS numeroComande,
           COALESCE(SUM(totale), 0) AS incasso
    FROM v_comande_completate
    WHERE dataChiusura >= p_giorno
      AND dataChiusura <  p_giorno + INTERVAL 1 DAY;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G8 - Entrate mensili.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_entrate_mensili$$
CREATE PROCEDURE sp_entrate_mensili(
    IN p_anno INT,
    IN p_mese INT
)
BEGIN
    DECLARE v_inizio DATE;

    SET v_inizio = MAKEDATE(p_anno, 1) + INTERVAL (p_mese - 1) MONTH;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT COUNT(*)                 AS numeroComande,
           COALESCE(SUM(totale), 0) AS incasso
    FROM v_comande_completate
    WHERE dataChiusura >= v_inizio
      AND dataChiusura <  v_inizio + INTERVAL 1 MONTH;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- G9 - Inserisce un nuovo prodotto oppure aggiorna quello esistente,
-- inclusa la sospensione temporanea della disponibilita.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_salva_prodotto$$
CREATE PROCEDURE sp_salva_prodotto(
    IN p_codice      SMALLINT UNSIGNED,
    IN p_nome        VARCHAR(60),
    IN p_prezzo      DECIMAL(5,2),
    IN p_tipo        ENUM('PIZZA','BEVANDA'),
    IN p_disponibile BOOLEAN
)
BEGIN
    IF p_codice IS NULL THEN
        INSERT INTO Prodotto (nome, prezzo, tipo, disponibile)
        VALUES (p_nome, p_prezzo, p_tipo, p_disponibile);
        SELECT LAST_INSERT_ID() AS codice;
    ELSE
        UPDATE Prodotto
        SET nome        = p_nome,
            prezzo      = p_prezzo,
            tipo        = p_tipo,
            disponibile = p_disponibile
        WHERE codice = p_codice;

        IF ROW_COUNT() = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Prodotto inesistente';
        END IF;
    END IF;
END$$


-- ---------------------------------------------------------------------
-- G10 - Gestione dei dipendenti. La password non transita mai in chiaro
-- oltre il confine della procedura. La rimozione e preclusa a chi ha
-- operazioni registrate, coerentemente con i vincoli di integrita
-- referenziale e con la stima dei volumi.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_registra_dipendente$$
CREATE PROCEDURE sp_registra_dipendente(
    IN p_username VARCHAR(32),
    IN p_password VARCHAR(64),
    IN p_nome     VARCHAR(40),
    IN p_cognome  VARCHAR(40),
    IN p_ruolo    ENUM('CAMERIERE','PIZZAIOLO','BARMAN','GESTORE')
)
BEGIN
    INSERT INTO Dipendente (username, password, nome, cognome, ruolo)
    VALUES (p_username, SHA2(p_password, 256), p_nome, p_cognome, p_ruolo);
END$$


DROP PROCEDURE IF EXISTS sp_rimuovi_dipendente$$
CREATE PROCEDURE sp_rimuovi_dipendente(
    IN p_username VARCHAR(32)
)
BEGIN
    IF EXISTS (SELECT 1 FROM Comanda WHERE usernameDipendente = p_username)
    OR EXISTS (SELECT 1 FROM ElementoComanda WHERE usernamePreparatore = p_username) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il dipendente ha operazioni registrate e non puo essere rimosso';
    END IF;

    DELETE FROM Dipendente WHERE username = p_username;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dipendente inesistente';
    END IF;
END$$


-- ---------------------------------------------------------------------
-- Procedura accessoria: menu corrente, di supporto a C2 e G3.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_lista_menu$$
CREATE PROCEDURE sp_lista_menu()
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT codice, nome, prezzo, tipo
    FROM Prodotto
    WHERE disponibile = TRUE
    ORDER BY tipo, nome;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- Procedura accessoria: elenco dei tavoli liberi, di supporto a C1.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_tavoli_liberi$$
CREATE PROCEDURE sp_tavoli_liberi()
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT numero, posti
    FROM Tavolo
    WHERE stato = 'LIBERO'
    ORDER BY numero;

    COMMIT;
END$$


-- ---------------------------------------------------------------------
-- Procedura accessoria: comande aperte, di supporto a C2, G3, G5 e G6.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_comande_aperte$$
CREATE PROCEDURE sp_comande_aperte()
BEGIN
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SET TRANSACTION READ ONLY;
    START TRANSACTION;

    SELECT ID, tipo, dataApertura, numeroTavolo, telefonoCliente, indirizzo
    FROM Comanda
    WHERE stato = 'APERTA'
    ORDER BY dataApertura;

    COMMIT;
END$$

DELIMITER ;


-- =====================================================================
-- 7. UTENTI APPLICATIVI E PRIVILEGI
--    Nessun privilegio di scrittura diretta sulle tabelle: gli utenti
--    possono soltanto eseguire le procedure di propria competenza.
--    Le procedure girano con i privilegi del definer (SQL SECURITY
--    DEFINER, comportamento predefinito).
-- =====================================================================

DROP USER IF EXISTS 'app_login'@'%';
DROP USER IF EXISTS 'app_cameriere'@'%';
DROP USER IF EXISTS 'app_pizzaiolo'@'%';
DROP USER IF EXISTS 'app_barman'@'%';
DROP USER IF EXISTS 'app_gestore'@'%';

-- Utente usato dall'applicazione prima dell'autenticazione, quando il ruolo
-- del dipendente non e ancora noto: puo eseguire soltanto sp_login e non ha
-- alcun accesso ai dati.
CREATE USER 'app_login'@'%'     IDENTIFIED BY 'login_pwd';
CREATE USER 'app_cameriere'@'%' IDENTIFIED BY 'cameriere_pwd';
CREATE USER 'app_pizzaiolo'@'%' IDENTIFIED BY 'pizzaiolo_pwd';
CREATE USER 'app_barman'@'%'    IDENTIFIED BY 'barman_pwd';
CREATE USER 'app_gestore'@'%'   IDENTIFIED BY 'gestore_pwd';

-- Pre-autenticazione
GRANT EXECUTE ON PROCEDURE pizzeria.sp_login                    TO 'app_login'@'%';

-- Cameriere
GRANT EXECUTE ON PROCEDURE pizzeria.sp_login                    TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_apri_comanda_locale      TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_aggiungi_elemento        TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_elementi_pronti_sala     TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_segna_elemento_consegnato TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_lista_menu               TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_tavoli_liberi            TO 'app_cameriere'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_comande_aperte           TO 'app_cameriere'@'%';

-- Pizzaiolo
GRANT EXECUTE ON PROCEDURE pizzeria.sp_login                    TO 'app_pizzaiolo'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_coda_preparazione        TO 'app_pizzaiolo'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_segna_elemento_preparato TO 'app_pizzaiolo'@'%';

-- Barman
GRANT EXECUTE ON PROCEDURE pizzeria.sp_login                    TO 'app_barman'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_coda_preparazione        TO 'app_barman'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_segna_elemento_preparato TO 'app_barman'@'%';

-- Gestore
GRANT EXECUTE ON PROCEDURE pizzeria.sp_login                    TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_apri_comanda_locale      TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_aggiungi_elemento        TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_elementi_pronti_sala     TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_segna_elemento_consegnato TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_lista_menu               TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_tavoli_liberi            TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_comande_aperte           TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_apri_comanda_asporto     TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_apri_comanda_consegna    TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_elementi_pronti_ritiro   TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_completa_comanda         TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_stampa_scontrino         TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_entrate_giornaliere      TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_entrate_mensili          TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_salva_prodotto           TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_registra_dipendente      TO 'app_gestore'@'%';
GRANT EXECUTE ON PROCEDURE pizzeria.sp_rimuovi_dipendente       TO 'app_gestore'@'%';

FLUSH PRIVILEGES;


-- =====================================================================
-- 8. DATI DI PROVA
-- =====================================================================

-- Venti tavoli, coerenti con la stima dei volumi.
INSERT INTO Tavolo (numero, posti) VALUES
 (1,2),(2,2),(3,4),(4,4),(5,4),(6,4),(7,4),(8,4),(9,4),(10,4),
 (11,4),(12,6),(13,6),(14,6),(15,6),(16,2),(17,2),(18,8),(19,8),(20,4);

-- Sette dipendenti: 1 gestore, 3 camerieri, 2 pizzaioli, 1 barman.
-- La password di prova coincide con l'username.
INSERT INTO Dipendente (username, password, nome, cognome, ruolo) VALUES
 ('mrossi',   SHA2('mrossi',   256), 'Mario',    'Rossi',    'GESTORE'),
 ('lbianchi', SHA2('lbianchi', 256), 'Luca',     'Bianchi',  'CAMERIERE'),
 ('gverdi',   SHA2('gverdi',   256), 'Giulia',   'Verdi',    'CAMERIERE'),
 ('aneri',    SHA2('aneri',    256), 'Anna',     'Neri',     'CAMERIERE'),
 ('sgallo',   SHA2('sgallo',   256), 'Salvatore','Gallo',    'PIZZAIOLO'),
 ('pesposito',SHA2('pesposito',256), 'Paolo',    'Esposito', 'PIZZAIOLO'),
 ('fconti',   SHA2('fconti',   256), 'Francesca','Conti',    'BARMAN');

-- Menu: pizze.
INSERT INTO Prodotto (nome, prezzo, tipo) VALUES
 ('Margherita',        6.00,  'PIZZA'),
 ('Marinara',          5.50,  'PIZZA'),
 ('Napoli',            7.00,  'PIZZA'),
 ('Diavola',           8.00,  'PIZZA'),
 ('Capricciosa',       9.00,  'PIZZA'),
 ('Quattro Stagioni',  9.00,  'PIZZA'),
 ('Quattro Formaggi',  8.50,  'PIZZA'),
 ('Bufala',            8.50,  'PIZZA'),
 ('Boscaiola',         9.50,  'PIZZA'),
 ('Ortolana',          8.00,  'PIZZA'),
 ('Prosciutto e Funghi', 8.50,'PIZZA'),
 ('Tonno e Cipolla',   8.00,  'PIZZA'),
 ('Calzone',           8.50,  'PIZZA'),
 ('Wurstel e Patatine',8.00,  'PIZZA'),
 ('Bianca',            5.00,  'PIZZA');

-- Menu: bevande.
INSERT INTO Prodotto (nome, prezzo, tipo) VALUES
 ('Acqua Naturale 0.5L',  1.50, 'BEVANDA'),
 ('Acqua Frizzante 0.5L', 1.50, 'BEVANDA'),
 ('Coca Cola 0.33L',      3.00, 'BEVANDA'),
 ('Fanta 0.33L',          3.00, 'BEVANDA'),
 ('Sprite 0.33L',         3.00, 'BEVANDA'),
 ('Birra Chiara 0.4L',    5.00, 'BEVANDA'),
 ('Birra Rossa 0.4L',     5.50, 'BEVANDA'),
 ('Birra Artigianale',    6.50, 'BEVANDA'),
 ('Vino della Casa 0.5L', 8.00, 'BEVANDA'),
 ('Caffe',                1.20, 'BEVANDA');

-- =====================================================================
-- Fine dello script.
-- =====================================================================
