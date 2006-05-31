#include <stdio.h>
#include <libiberty.h>

int main (int argc, char** argv) {
    char *cp;
    int opt = 0;
    if (argc == 1) return;
    hex_init();
    cp = argv[1];
    while (*cp) {
        if (*cp == '%' && hex_p(cp[1]) && hex_p(cp[2])) {
            putchar(hex_value(cp[1]) << 4 |
                    hex_value(cp[2]));
            cp+=3;
            continue;
        }
        if ((*cp == '+' || *cp == '&') && opt != 0)
            putchar(' ');
        else if (opt != 0)
            putchar(*cp);
        if (*cp == '=' || *cp == '&')
            opt = !opt;
        ++cp;
    }
    fflush(stdout);
}
    
