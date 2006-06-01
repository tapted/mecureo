#include <stdio.h>

int main(int argc, char** argv) {
  if (argc <= 1)
    return 0;
  printf("Content-Type: text/html\n\n"
         "<HTML>\n"
         "  <FRAMESET COLS=\"350,*" BORDER=0 FRAMEBORDER=0>\n"
         "    <FRAME SRC=\"leftvlum.cgi?%s\" NAME=vlum NORESIZE SCROLLING=no MARGINWIDTH=0 MARGINHEIGHT=0 FRAMEBORDER=0 border=0>\n"
         "    <FRAME SRC="http://foldoc.doc.ic.ac.uk/foldoc/foldoc.cgi?query=declarative%20language" NAME=topicframe NORESIZE MARGINWIDTH=5 MARGINHEIGHT=5 FRAMEBORDER=0 border=0>\n"
         "  </FRAMESET>\n"
         "</HTML>\n"
        ,
         argv[1]
        );
}
