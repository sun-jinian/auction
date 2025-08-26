#import "../lib.typ": *

= Filter mappings

#double_col_spaces(1cm)

#seq_diagram(
  "UserChecker filter",
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "UserChecker")
    _par("D", display-name: "Session")
    _par("C", display-name: "HomePage")

    _seq("A", "B", enable-dst: true, comment: [Login `||` Register])
    _seq("B", "D", enable-dst: true, comment: [getAttribute ("user")])
    _seq("D", "B", disable-src: true, comment: [return user])
    _seq("B", "C", disable-src: true, comment: [[user != null] ? redirect])
  }),
  comment: [
    The `UserChecker` filter checks, once the client accesses the Login or Register webpage, if the User is logged in.

    #v(0.1em)

    If that's the case, then the program redirects to the HomePage. If not, then the `InvalidUserChecker` filter comes in.
  ],
  next_page: false,
  comment_next_page_: false,
)

#double_col_spaces(1cm)

#seq_diagram(
  "InvalidUserChecker  filter",
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "InvalidUserChecker")
    _par("D", display-name: "Session")
    _par("C", display-name: "Login")

    _seq("A", "B", enable-dst: true, comment: [HomePage `||` Track `||` Logout `||` ...])
    _seq("B", "D", enable-dst: true, comment: [getAttribute ("user")])
    _seq("D", "B", disable-src: true, comment: [return user])
    _seq("B", "C", disable-src: true, comment: [[user == null] ? redirect])
  }),
  comment: [
    The `InvalidUserChecker` filter does the exact opposite of `UserChecker`. If the client accesses all the other pages -- HomePage, PlaylistPage, Track, Logout... -- and _is not logged in_, then the program redirects to the Login page.
  ],
  next_page: false,
  comment_next_page_: false,
)

// #balance([
//   The `PlaylistChecker` and `TrackChecker` filters are different from the precedent two because they don't simply implement the `Filter` interface, but instead extends `HttpFilter` class: this will become explained later.
// ])

#pagebreak()

#seq_diagram(
  "PlaylistChecker filter",
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "PlaylistChecker")
    _par("D", display-name: "Session")
    _par("F", display-name: "Request")
    _par("C", display-name: "PlaylistDAO")
    _par("E", display-name: `ERROR 403`, color: red.lighten(50%))

    _seq("A", "B", enable-dst: true, comment: [Playlist `||` AddTracks])
    _seq("B", "D", enable-dst: true, comment: [getAttribute ("user")])
    _seq("D", "B", disable-src: true, comment: [return user])
    _seq("B", "F", enable-dst: true, comment: [getParameter ("trackId")])
    _seq("F", "B", disable-src: true, comment: [return playlistId])
    _seq("B", "C", enable-dst: true, comment: [checkPlaylistOwner (playlistId, user)])
    _seq("C", "B", disable-src: true, comment: [return result])
    _seq("B", "E", disable-src: true, comment: [[result == false] ? sendError ("Playlist does not exist")])
  }),
  comment: [
    The `PlaylistChecker` filter is invoked in two scenarios: after the User has clicked on a playlist on HomePage (@playlistpage-sequence) and when uploading a track (@uploadtrack-sequence).

    It is in charge of checking if the requested playlist _actually belongs_ to the User requesting or trying to upload it. This is done via obtaining the User attribute from the session -- which is impossibile without extending the `HttpServlet` or `HttpFilter` classes -- and getting the needed paramaters from the request.

    Finally, a query is performed against the database. If the result is false, then the server will respond with `ERROR 403: forbidden`.
  ],
  next_page: true,
  comment_next_page_: false,
)

#seq_diagram(
  "SelectedTracksChecker filter",
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "SelectedTracksChecker")
    _par("D", display-name: "Session")
    _par("F", display-name: "Request")
    _par("C", display-name: "TrackDAO")
    _par("E", display-name: `ERROR 403`, color: red.lighten(50%))

    _seq("A", "B", enable-dst: true, comment: [CreatePlaylist `||` AddTracks])
    _seq("B", "D", enable-dst: true, comment: [getAttribute ("user")])
    _seq("D", "B", disable-src: true, comment: [return user])
    _seq("B", "F", enable-dst: true, comment: [getParameter ("selectedTracks")])
    _seq("F", "B", disable-src: true, comment: [return selectedTracks])
    _seq("B", "C", enable-dst: true, comment: [for trackId in selectedTracks \ checkTrackOwner (trackId, user)])
    _seq("C", "B", disable-src: true, comment: [return isOwner])
    _seq("B", "E", disable-src: true, comment: [[isOwner == false] ? sendError ("Track does not exist")])
  }),
  comment: [
     The `SelectedTracksChecker` filter is invoked in two scenarios too: during the creation of a playlist (@createplaylist-sequence) and during the UploadTrack sequence (@uploadtrack-sequence).

    `SelectedTracksChecker` applies a very similar pipeline to c`PlaylistChecker`: instead of checking the playlist, it does the same job but for one of more tracks when the User requests to add them to a playlist.

    Again similarly to `PlaylistChecker`, it also obtains the User attribute from the session and the needed parameters; if the User does not have access rights to the requested track(s), the response is `ERROR 403`.
  ],
  next_page: false,
  comment_next_page_: false,
  label_: "selectedtrackschecker-filter",
)

#pagebreak()

#seq_diagram(
  "TrackChecker filter",
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "TrackChecker")
    _par("D", display-name: "Session")
    _par("F", display-name: "Request")
    _par("C", display-name: "TrackDAO")
    _par("E", display-name: `ERROR 403`, color: red.lighten(50%))

    _seq("A", "B", enable-dst: true, comment: [Track `||` PlaylistPage])
    _seq("B", "D", enable-dst: true, comment: [getAttribute ("user")])
    _seq("D", "B", disable-src: true, comment: [return user])
    _seq("B", "F", enable-dst: true, comment: [getParameter ("trackId")])
    _seq("F", "B", disable-src: true, comment: [return trackId])
    _seq("B", "C", enable-dst: true, comment: [checkTrackOwner (trackId, user)])
    _seq("C", "B", disable-src: true, comment: [return isOwner])
    _seq("B", "E", disable-src: true, comment: [[result == false] ? sendError("Track does not exist")])
  }),
  comment: [
    Finally the `TrackChecker` filter does the same exact job as `SelectedTracksChcker`, but for a single track once a User presses the corresponding button in the `playlist_page` (see @track-sequence).
  ],
  next_page: false,
  comment_next_page_: false,
  label_: "trackchecker-filter",
)
