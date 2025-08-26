#import "../lib.typ": *

= Pipeline

#import "@preview/lovelace:0.3.0": *

#figure(
  placement: top,
  scope: "parent",
  pseudocode-list(indentation: 1.8cm)[
    + #s_action Server startup
    + #s_action LoginPage loading
    + #u_action User login
    + #u_action User accesses HomePage
    + `switch(user_action)`:
      // Non ha senso che l'utente aggiunga una track nella schermata delle playlists
      // + #u_action `add_track`
      //   + #s_action `form_add_track` $==>$ #addon `button_add_track`
      + #u_action `create_playlist`
        + #s_action `form_add_playlist`$==>$ #addon `button_add_playlist`
      + #u_action `open_playlist`
        + #pg `playlist_page`
        + `switch(user_action)`:
          + #u_action `listen_track` #comment("adds all playlist tracks to queue")
            + #pg `player_page`
            + #s_action `audio_player`
          + #u_action `add_track`
            + #s_action `form_add_track` $==>$ #addon `button_add_track`
          + #addon #u_action `remove_track`
            + #addon `button_remove_track`
      // + #addon #u_action `all_tracks` #comment("goes to the ALL tracks page")
      //   + #pg `tracks_page`
      //   + `switch (user_action)`:
      //     + #addon #u_action `remove_track`
      //       + #addon `button_remove_track`
      //     + #u_action `listen_track` #comment("adds ALL tracks to the queue")
      //       + #pg `player_page`
      //       + #s_action `audio_player`
      + #addon #u_action `logout`
        + #addon #s_action `logout_page`
      + `upload_track`
    + #s_action LoginPage loading
    + `goto 3`
  ],
)
