#import "../lib.typ":*

= Cut content<cut-content>

During the development, we had many ideas and thought about ways to implement them -- however, due to time and work restrictions, some features didn't make it to the final release. They can be categorized in features, optimizations and code cleanup.

== Features

In regards to the features, we wanted to implement *Next/Previous* buttons for the Playlists too, to make the application behave in a more coherent way: according to the submission, only the tracks in the playlist must implement it. Following this cohesion, the same can be said about the *Delete* functionalities: initially, along with the creation of a track/playlist we wanted to add a delete option -- if you add something, you might want to remove it at a later date.

To comply with best practises, since the application does offer a logout button -- which is an added feature, not required -- it should also provide a user profile window; or at least welcome the user upon login.

/ JS: For the JavaScript project -- which is not correct to call like that, since we used TypeScript -- there were plans to implement a localization function, similar to how the HTML project works. It would have been a parser for the `.properties` files already created: they would have been recycled. The most ambituos idea was, however, to deploy the JavaScript project to Github pages. This is not possible with thymeleaf since it needs a server running at all times, but with JavaScript running the in client... it was perfect. To access the database, we planned to use SQL.js @sql-js and #sqlite() @sqlite.

/ CSS frameworks: We all know CSS is awesome and very powerful, however, as is the case with many technologies, its usage in a raw form is often negleted: as no one dares to write in #text(font: "New Computer Modern")[plain] #TeX because #LaTeX exists, software like Hibernate (more on this later) abstract the SQL from the developer, the same applies to CSS. In the wild there are many frameworks -- Tailwind-CSS, Sass just to name a few. We wanted to have our fair share and use Bulma @bulma; in the end, we wrote everything ourselves.

== Optimizations<cut-content-optimizations>

/ The OG database : The first database implementation was created with a different logic than the one we ended up with. We thought that the tracks were a common pool, similarly to all the tracks of a streaming service, and then each user could select some among them. In this way, if a user wanted to upload a track but it had already been uploaded by someone else, the server would have just linked that track to the current user -- the reason behind was to optimize track storage and forbid duplicates.

To support such a logic, there used to exist a `track` and a `user_tracks` tables. This allowed us to perform some further optimization -- we had thought about of creating a trigger in the database: it would have deleted a track from the corresponding table if that track wasn't associated with at least a user (in the ER diagram it was a weak entity, that is it existed only as long as there was a link).

The issue was quite simple... the submission _didn't specify_ this; instead, every user has their tracks. They can be the same exact files of another user -- pretty much like how a cloud service behaves. And that's how the project works. Still, we couldn't just let the user upload track at will without some checks. And that trigger evoled in the path and image checksums attributes.

/ Missing hashing: One could argue: "You went above and beyond to ensure the user doesn't upload the same exact file multiple times, yet you don't even hash the passwords". And you would be right. We wanted to do that by leveraging the power of Password4J @password4j, but once again the specification didn't ask for it and so... we had other features to work on.

/ Connection pooling : Another important optimization technique is #link("https://en.wikipedia.org/wiki/Connection_pool")[connection pooling]: to put it simply, instead of opening every time a new connection to the database -- which is the most computationally expensive operation database-wise -- there is a pool of reusable connections, that are always open. This way, the database is accessed once and then the queries are performed by those same connections. Our library of choice was HikariCP @hikaricp.

/ ORM: The proper (or #emph[elegant]) way to interact with the database isn't by directly writing raw SQL code but by using APIs written for this very reason. There are many examples in web techologies -- such as jQuery -- though for the Java programming language, pioneer of the Object-Oriented Programming paradigma, there is a more potent concept: #link("https://en.wikipedia.org/wiki/Object-relational_mapping")[Object Relational Mapping (ORM)]. As the name suggests, a relational object is mapped to a Java object. By using Hibernate @hibernate a table could be mapped 1:1 to a class and its attributes: every query -- select, insert, delete... -- can be performed through it with commits, transactions and so on.

/ Springtrap: Probably the saddest turn back was not being able to use the Spring Boot framework @spring-boot, which is commonly used. It's a framework to create production-level applications: as such, it's definitely a must, whatever the future may hold. Also, during research of how thymeleaf operates, it was basically _always_ paired with Spring Boot.

/ Caching: If a playlist doesn't change and the user requests it again, there should be no reason to make another GET request to the server. Instead, it could be cached. We didn't search for specific software to do so: it remained a idea.

== Code cleanup

The last scrapped ideas were all about some refactoring here and there:

- Thymeleaf can process the parameters value directly, effectively bypassing the context setting: this could potentially reduce the boilerplate and make for a more elegant code

- Since the Record classes are immutable, in order to get them to work without settings all nulls there have been some workarounds what could be rewritten and polished

- The `homepage.ts` file is not at all easily navigabile -- we often found ourselves scrolling up and down because it's 1000+ pages long -- though it's thoroughly commented: we wanted to dismember it in separate files (`homepage.ts`, `playlist.ts`, `playerpage.ts`) to fix this, but due to the use of global variables and time we weren't able to do it

- The `createModal` function was being created in parallel with the `track-reorder` modal in different branches, thus the latter couldn't have been generated with the former and now there is some code duplication; this could have happened either way because this particular modal is quite different from the others
