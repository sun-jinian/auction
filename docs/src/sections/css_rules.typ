#import "../lib.typ": *

= Cascading Style Sheets (CSS) styling<css-styling>

== Introduction

The project is based on a single CSS file, `components.css`, and all the others rely upon it to retrieve the styles. Furthermore, all the colours are sourced from the `colors.css` file, which is based on #emph[tinted-theming] @tinted-theming, a collection of commonly used themes in the developing world. We have chosen to use the #emph[Classic Light theme]#footnote[This very documentation is also sourced from that same colourscheme.].

If you want to change the overall theme of the website, just switch to a new colorscheme by looking at the #link("https://tinted-theming.github.io/tinted-gallery/")[tinted-theming gallery]. In `colors.css` there are a few commented styles to choose from.

#css_explanation(
  ```css
  body {
      background-color: var(--default-background);
      padding: 1rem 2rem 2rem 2rem;
      line-height: 1.6;
      word-spacing: 1px;
      font-family: "JetBrains Mono", monospace;
      height: 100vh;
      text-overflow: ellipsis;
  }
  ```,
  [
    As stated earlier, the `background-color` is sourced from the `colors.css`. Then the padding is always `2rem`, except above, where it's `1rem`. The text is able to wrap thanks to `ellipsis` option on `text-overflow`.
  ],
)

After the body, we styled all the elements in a consistent manner.

== Buttons

#css_explanation(
  ```css
  .button {
      color: var(--selection-background);
      background-color: var(--default-foreground);
      border: 2px solid var(--dark-foreground);
      height: 3rem;
      border-radius: 6px;
      font-weight: bold;
      vertical-align: middle;
      margin: 0.5rem 0 0.5rem 0;
      padding: 1em;
      font-family: "JetBrains Mono", monospace;
  }
  ```,
  [
    Every button is derived from the one above. The text is aligned in the center both horizontally and vertically; its weight set to bold. Then there are some margin and padding to help the user see better#footnote[There will be an exception later.].
  ],
)

A notable exception to the buttons colorscheme is the `logout` button:
#css_explanation(
  ```css
  .logout {
      background-color: var(--variables);
      font-weight: bolder;
      color: var(--lighter-background);
  }

  .logout:hover {
      background-color: var(--data-types);
  }
  ```,
  [
    Both the `background-color`, `font-weight` and `color` are different, to further imply that the logout button is different from the others (upload track, create playlist...).
  ],
)

// The same can be said for the `close` button in the modal, which will be explained after.

== Containers

The first container the user sees is the Login one, which shares its design with Register and the track player:
#css_explanation(
  ```css
  .center-panel {
      width: 300px;
      background-color: var(--lighter-background);
      border: 1px solid var(--dark-foreground);
      padding: 3rem;
      text-align: center;
  }
  ```,
  [
    An important aspect of login and register is their horizontal bar:
    #emph[
      ```css
      hr {
        display: block;
        height: 1px;
        border: 0;
        border-top: 1px solid var(--light-background);
        margin: 1em 0;
        padding: 0;
      }
      ```
    ]
    which is not used in the track player.
  ],
)

Some very basic functions of a playlist manager is being able to display all the playlists and tracks of a given user. To achieve that, we opted for a classic layout composed of a top and bottom navigation bars and a main, central section.

#css_explanation(
  ```css
  .nav-bar {
      width: 100%;
      margin: 0;
      display: flex;
      flex-wrap: wrap;
      align-content: space-around;
      justify-content: center;
      align-items: center;
      gap: 1rem;
  }
  ```,
  [
    The navigation bar is the same both above and below. It's a flex container because it's important to have a flexible container for the main-title (e.g. "All Playlists") and the buttons (with a variable number between screens).

    The layout is computed as follows:
    #table(
      columns: (auto, 1fr) + (auto,) * 3,
      stroke: silver,
      align: center,
      `title`,
      table.cell(fill: purple.transparentize(50%), `spacer`),
      `button`,
      `button`,
      `logout`,
    )
    so we created the `spacer` element:

    #colbreak()

    #emph[
      ```css
      .spacer {
          flex-grow: 1;
      }
      ```
    ]
    which takes all the space available.
  ],
)

Next, the tracks and playlists containers.

#css_explanation(
  ```css
  .items-container {
      width: 100%;
      display: grid;
      grid-template-columns: 1fr 1fr 1fr 1fr 1fr;
      align-content: baseline;
      justify-content: center;
      gap: 1rem;
      padding: 1rem 0 1rem 0;
  }

  .single-item {
      display: flex;
      flex-wrap: nowrap;
      background-color: var(--light-background);
      border: 2px solid var(--data-types);
      border-radius: 5px;
      color: var(--lighter-background);
      padding: 1rem;
      height: 150px;
      font-family: "JetBrains Mono", monospace;
      font-weight: 700;
      text-align: left;
      align-content: end;
      align-items: end;
      justify-content: space-between;
  }

  .single-item:hover {
      background-color: var(--variables);
      cursor: pointer;
  }
  ```,
  [
    According to project the specifications (@project-breakdown), there must be #emph[at most 5 tracks per page]: we opted for a CSS grid. This works well along with the `body` previously set because the grid can expand and shrink its items accordingly.

    As per the navigation bar, the single items are themselves flexible boxes. The difference lies in the fact they are not allowed to wrap -- one might ask: why not, since the tracks must list both track title and album title? because we handle that line break manually with the `<br>` tag.
  ],
)

Last but not least, the errors.

#css_explanation(
  ```css
  .error{
      color: var(--variables);
      padding-top: 0.5rem;
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      align-content: space-around;
      justify-content: center;
      align-items: center;
  }
  ```,
  [
    When the User tries to do something forbidden -- adding duplicate tracks, creating a duplicate playlist... -- an error will appear. It's exclusively used in the modal and due to how it's spaced it requires the `flex` display.

    The simpler implementation is `sql-error`:
    #emph[
      ```css
      .sql-error {
          color: var(--variables)
      }
      ```
    ]
    which is used during registration.
  ],
)

// #colbreak()

== Modal<css-modal>

/ RIA version : The modals in the RIA project are dynamically generated via Javascript only when needed and removed when the view is changed. This section has been written with the HTML version in mind.

Finally, undoubtedly the most difficult CSS component in this project to comprehend is the modal, which is a dialog window created entirely with CSS.

A complex element, it can be broken in multiple parts:

- The window
```css
.modal-window {
    position: fixed;
    background-color: rgba(255, 255, 255, 0.25);
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 999;
    visibility: hidden;
    pointer-events: none;
    transition: all 0.5s;
}
```
it's `hidden` by default, but once it's invoked it must be be above everything -- this is handled by the `z-index` property. Its position must be `fixed`, since it's not a movable window; also it can't be targeted by the cursor: `pointer-events` are none. Another key aspect is the background color: in order to make it stand from its background, a slight blurred white is needed (see @css-modal-representation).
#figure(
  scope: "parent",
  placement: bottom,
  context {
    let height = 3cm
    let width = 7cm
    let spacing = 1.25cm
    stack(
      dir: ltr,
      spacing: spacing,
      rect(
        width: width,
        height: height,
        fill: get-comments(),
        align(
          start,
          "nav-bar",
        ),
      ),
      rect(
        width: width,
        height: height,
        fill: get-comments(),
        align(
          start,
          "nav-bar",
        )
          + place(
            center + horizon,
            rect(
              width: width,
              height: height,
              fill: rgb(255, 255, 255, 25%),
              box(
                inset: 4pt,
                stroke: get-variables(),
                fill: get-lighter-background(),
                "modal-window",
              ),
            ),
          ),
      ),
    )
  },
  caption: [Modal representation.],
)<css-modal-representation>

// #pagebreak()

- The target, when the user presses a button that launches the modal (e.g. Upload Track)

```css
.modal-window:target {
    visibility: visible;
    opacity: 1;
    pointer-events: auto;
}

.modal-window > div {
    width: 400px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    padding: 1em;
    background: var(--lighter-background);
    border: 2px solid var(--variables);
}
```
once the modal has been invoked, its visibility must be switched to `visible` and `opacity` to 1. The child element `div` of the window must be at the center of the screen, both horizontally and vertically: this is managed with the `top`, `left` and `translate` properties.

- The close button
```css
.modal-close {
    color: var(--lighter-background);
    background-color: var(--variables);
    border-radius: 5px;
    position: absolute;
    top: 2%;
    right: 2%;
    cursor: pointer;
    padding: 0.2rem;
    font-size: 0.8rem;
    font-weight: bold;
    text-align: center;
    text-decoration: none;
}

.modal-close:hover {
    color: black;
}
```
as stated previously, the `modal-close` button is an exception to the `button` rule. It's considerably smaller than the others, the cursor is a `pointer`. Its position is computed on the `modal-window`, from the above right.

- The dropdown menus
```css
select:invalid {
    color: #505050;
}
```
this pseudoclass causes the color of the placeholder in dropdown menus (Year, Genres) to be gray, as regular placeholders should be #footnote[Otherwise it would have been black as the text, which is not aesthetically pleasant.].

== #ria() Sidebar<ria-css-sidebar>

This components exists only in the RIA project, where we implemented a lateral bar that contains three buttons: Homepage, Playlist and Track. It effectively acts as an upgraded version of the bottom navigation bar of the HTML project.

#css_explanation(
  ```css
  .side-bar {
      height: 100%;
      width: 8rem;
      position: fixed;
      z-index: 1;
      left: 0;
      top: 0;
      background-color: var(--light-background);
      overflow: hidden; /* disable scrolling */
      display: flex;
      flex-direction: column;
      justify-content: start; /* buttons vertical alignment */
  }
  ```,
  [
    It always sits on the left side, with a 100% height. Its position can't be changed and neither its content. The background acts as contrast with the playlist tracks buttons.
  ]
)

Then the styling of the menus.

#css_explanation(
  ```css
  .side-bar .entry-button {
      margin: 1rem;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      align-items: center;
  }

  .entry-button:hover {
      cursor: pointer;
  }

  .entry-button .icon {
      width: 100%;
      background-color: transparent;
      border-color: blue;
      border-width: 1cm;
  }

  .entry-button .title {
      font-family: "JetBrains Mono", monospace;
      font-size: 100%;
  }
  ```,
  [
    The menus are of `entry-button` class, which can exist _only_ in a `side-bar` container (as seen in the first line of the previous source code extract). They are horizontally centered.

    The class `icon`, `.title` can exist only inside the `entry-button` -- thus they can exist only inside the `side-bar` class.
  ]
)

And lastly, legacy code.

#css_explanation(
  ```css
  .side-bar .close-button {
      color: var(--lighter-background);
      background-color: var(--variables);
      border-radius: 5px;
      font-family: "JetBrains Mono", monospace;
      position: absolute;
      top: 0.5rem;
      right: 2rem;
  }

  .side-bar .close-button:hover {
      color: var(--dark-foreground);
      cursor: pointer;
  }
  ```,
  [
    Originally the side bar was supposed to open and close, like a lateral menu as can be seen in many mobile apps -- that's why we needed a close button. For the current implementation, it's not used.
  ]
)
