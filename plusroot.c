#include <stdio.h>

int main (int argc, char** argv) {
  char * p=argv[1];
  for (; *p != '='; ++p);
  for (++p; *p != '='; ++p);
  ++p;
  if (*p == '%') p+=3;
  {
    char * q = p;
    for (; *q != '&'; ++q);
    if (*(q-3) == '%') q-=3;
    *q = '\0';
  }
  printf(p);
}
