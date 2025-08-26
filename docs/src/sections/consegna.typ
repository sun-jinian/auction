#import "../lib.typ":*

= Consegna in 🇮🇹

Un'applicazione web consente la gestione di una playlist di brani musicali. Playlist e brani sono personali di ogni utente e non condivisi.

== Base di dati

Ogni #entity[utente] ha #attr[username], #attr[password], #attr[nome] e #attr[cognome]. Ogni #entity[brano musicale] è memorizzato nella base di dati mediante un #attr[titolo], l‘#attr[immagine] e il #attr[titolo dell’album] da cui il brano è tratto, il #attr[nome dell’interprete] (singolo o gruppo) dell’album, l’#attr[anno di pubblicazione] dell’album, il #attr[genere] musicale e il #attr[file musicale]. Inoltre:
- Si supponga che i #attr_spec[generi siano prefissati]

- Non è richiesto di memorizzare l’ordine con cui i brani compaiono nell’album a cui appartengono

- Si ipotizzi che un brano possa appartenere a un solo album (no compilation)

L’utente, previo login, #rel[può creare brani mediante il caricamento dei dati relativi e raggrupparli in playlist]. Una  #entity[playlist] è un insieme di #attr_spec[brani scelti tra quelli caricati dallo stesso utente]. #attr_spec[Lo stesso brano può essere inserito in più playlist]. Una playlist ha un #attr[titolo] e una #attr[data di creazione] ed è #attr[associata al suo creatore].

// #show "form": it => underline(stroke: aqua + 1.5pt, it)

== Comportamento

A seguito del login, l’utente #user_action[accede] all’#page[HOME PAGE] che presenta l’elenco delle proprie playlist, ordinate per data di creazione decrescente, un #element[form per caricare un brano] con tutti i dati relativi e un #element[form per creare una nuova playlist].

- Il form per la creazione di una nuova playlist #server_action[mostra] l’elenco dei brani dell’utente ordinati per ordine alfabetico crescente dell’autore o gruppo e per data crescente di pubblicazione dell’abum a cui il brano appartiene

- Tramite il form è possibile #user_action[selezionare uno o più brani da includere]

Quando l’utente #user_action[clicca su una playlist] nell’#page[HOME PAGE], appare la pagina #page[PLAYLIST PAGE] che contiene inizialmente una tabella di una riga e cinque colonne.

- Ogni cella contiene il titolo di un brano e l’immagine dell’album da cui proviene

- I brani sono ordinati da sinistra a destra per ordine alfabetico crescente dell’autore ogruppo e per data crescente di pubblicazione dell’abum a cui il brano appartiene

- Se la playlist contiene più di cinque brani, sono disponibili comandi per vedere il precedente e successivo gruppo di brani

/ Navigazione brani playlist: Se la pagina #page[PLAYLIST]:

+ Mostra il #emph[primo gruppo] e ne esistono altri successivi nell’ordinamento, #strong[compare a destra della riga il bottone SUCCESSIVI], che permette di vedere il gruppo successivo

+ Mostra l’#emph[ultimo gruppo] e ne esistono altri precedenti nell’ordinamento, #strong[compare a sinistra della riga il bottone PRECEDENTI], che permette di vedere i cinquebrani precedenti

+ #emph[Mostra un blocco e esistono sia precedenti sia successivi], compare a destra della riga il bottone SUCCESSIVI e a sinistra il bottone PRECEDENTI

/ Aggiunta brani : La pagina #page[PLAYLIST] contiene anche un #element[form che consente di selezionare e aggiungere uno o più brani alla playlist corrente, se non già presente nella playlist]. Tale form presenta i brani da scegliere nello stesso modo del form usato per creare una playlist.

A seguito dell’aggiunta di un brano alla playlist corrente, l’applicazione #server_action[visualizza nuovamente la pagina] a #strong[partire dal primo blocco] della playlist. Quando l’utente #user_action[seleziona il titolo di un brano], la pagina #page[PLAYER] #server_action[mostra] tutti i #element[dati del brano scelto] e il #element[player audio per la riproduzione del brano].
