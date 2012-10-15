#pragma version(1)
#pragma rs java_package_name(com.jooyunghan.mandelbrot)
 
#include "rs_graphics.rsh"

#define ITERATION 1000


double2 d_begin;
double2 i_begin;
double  scale;

uchar4 black;
uchar4 white;

void init() {
 black = rsPackColorTo8888(0, 0, 0);
 white = rsPackColorTo8888(1, 1, 1);
}

void root(uchar4 *v_out,  uint32_t x, uint32_t y) {
    double2 i = { x, y };
    double2 d = (i_begin + i) / scale + d_begin;
    
    if (d.x < -2.0 || d.x > 1.0 || d.y < -1.0 || d.y > 1.0) {
        *v_out = white;
        return;
    }

   // iterate
    double2 z = { 0, 0 };
    int iter = 0;
    while (iter < ITERATION && z.x * z.x + z.y * z.y < 4.0) {
        double2 z_1 = {z.x * z.x - z.y * z.y + d.x, 2 * z.x * z.y + d.y};
        z = z_1;
        iter++;
    }

    if (iter == ITERATION) { // (dx, dy) is in mandelbrot set
        *v_out = black;
    } else { // out, then use iter for color
        *v_out = white;
    }
}