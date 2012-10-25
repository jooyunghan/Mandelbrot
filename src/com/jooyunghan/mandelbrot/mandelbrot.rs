#pragma version(1)
#pragma rs java_package_name(com.jooyunghan.mandelbrot)
 
#include "rs_graphics.rsh"

#define ITERATION 1000
#define COLORS    10

double2 d_begin;
double  scale;

uchar4 color_in;
uchar4 color_out;
//uchar4 white[COLORS];

void init() {
 color_in = rsPackColorTo8888(0, 0, 0);
 color_out = rsPackColorTo8888(1, 1, 1);
// for (int i=0; i<COLORS; i++) {
// 	white[i] = rsPackColorTo8888(i/(double)(COLORS-1), i/(double)(COLORS-1), 1);
// }
}

void root(uchar4 *v_out,  uint32_t x, uint32_t y) {
    double2 i = { x, y };
    double2 d = i / scale + d_begin;

   // iterate
    double2 z = { 0, 0 };
    int iter = 0;
    while (iter < ITERATION && z.x * z.x + z.y * z.y < 4.0) {
        double2 z_1 = {z.x * z.x - z.y * z.y + d.x, 2 * z.x * z.y + d.y};
        z = z_1;
        iter++;
    }

    if (iter == ITERATION) { // (dx, dy) is in mandelbrot set
        *v_out = color_in;
    } else { // out, then use iter for color
        *v_out = color_out;
    }
}