#import "lib.typ": *
#import "properties.typ": *

#set-theme(yaml("classic-light.yaml"))

#show: project.with(..properties)

#show: frontmatter.with()
#include "sections/abstract.typ"

#show: mainmatter.with()
#include "sections/submission.typ"
#include "sections/specification_completion.typ"
#include "sections/sql.typ"
#include "sections/components.typ"
#include "sections/sequence_diagram.typ"