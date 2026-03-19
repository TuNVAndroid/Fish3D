precision highp float;

varying vec2 v_texCoords;
varying vec4 v_position;
uniform sampler2D u_texture;
//uniform float u_time;
//uniform vec2 u_resolution;

uniform sampler2D u_map;
uniform float u_pixel;

uniform int u_softShadows;

/*const float PI = 3.1415926535897932;

// play with these parameters to custimize the idle effect
// ===================================================

// speed
const float speed = 0.2;
const float speed_x = 0.02;
const float speed_y = 0.03;

// refraction
const float emboss = 0.1;
const float intensity = 0.5;
const int steps = 2;
const float frequency = 10.0;
const int angle = 7; // better when a prime

// reflection
const float delta = 60.;
const float intence = 700.;

const float reflectionCutOff = 0.012;
const float reflectionIntence = 200000.;

// ===================================================


float col(vec2 coord, float time)
{
    float delta_theta = 2.0 * PI / float(angle);
    float col = 0.0;
    float theta = 0.0;
    for (int i = 0; i < steps; i++)
    {
        vec2 adjc = coord;
        theta = delta_theta*float(i);
        adjc.x += cos(theta)*time*speed + time * speed_x;
        adjc.y -= sin(theta)*time*speed - time * speed_y;
        col = col + cos( (adjc.x*cos(theta) - adjc.y*sin(theta))*frequency)*intensity;
    }

    return cos(col);
}*/

float sin2(float param)
{
    //return texture2D(u_map, vec2(param)).r;
    return param;
}

float asin2(float param)
{
    //return texture2D(u_map, vec2(param)).r;
    return param;
}

float tan2(float param)
{
    //return texture2D(u_map, vec2(param)).r;
    return param;
}

float atan2(float param)
{
    //return texture2D(u_map, vec2(param)).r;
    return param;
}


//---------- main

void main()
{
    //
    // water idle
    //
    /*float time = u_time * 1.3;

    vec2 p = v_position.xy/u_resolution, c1 = p, c2 = p;
    float alpha = 1.0;
    float cc1 = col(c1,time);

    c2.x += u_resolution.x/delta;
    float dx = emboss*(cc1-col(c2,time))/delta;

    c2.x = p.x;
    c2.y += u_resolution.y/delta;
    float dy = emboss*(cc1-col(c2,time))/delta;

    c1.x += dx*2.0;
    c1.y = -(c1.y+dy*2.0);

    alpha = 1.0+dot(dx,dy)*intence;

    float ddx = dx - reflectionCutOff;
    float ddy = dy - reflectionCutOff;
    if (ddx > 0.0 && ddy > 0.0)
        alpha = pow(alpha, ddx*ddy*reflectionIntence);

    vec4 col = texture2D(u_texture,c1)*(alpha);*/

    //
    // water tap
    //
    vec2 c1 = v_texCoords;
    float r_index = 2.0;

    float r = texture2D(u_map, vec2(v_texCoords.s, v_texCoords.t)).r;
    vec2 diff = vec2(texture2D(u_map, vec2(v_texCoords.s + u_pixel, v_texCoords.t)).r - r,
                     texture2D(u_map, vec2(v_texCoords.s, v_texCoords.t + u_pixel)).r - r);

    vec2 angle = vec2(atan2(diff.x), atan2(diff.y));
    vec2 refraction = vec2(asin2(sin2(diff.x) / r_index), asin2(sin2(diff.y) / r_index));
    vec2 displacement = vec2(tan2(refraction.x) * diff.x, tan2(refraction.y) * diff.y);

    vec2 d_tex_coord;
    if (diff.x < 0.0) {
        // current position is higher - clockwise rotation
        if (diff.y < 0.0) {
            d_tex_coord = -displacement;
        } else {
            d_tex_coord = vec2(-displacement.x, displacement.y);
        }
    } else {
        // current position is lower - counterclockwise rotation
        if (diff.y < 0.0) {
            d_tex_coord = vec2(displacement.x, -displacement.y);
        } else {
            d_tex_coord = displacement;
        }
    }
    d_tex_coord *= 40.0;
    vec4 col = texture2D(u_texture, c1 + d_tex_coord);
    col.rgb += clamp(r - 0.5, -0.01, 1.0) * 1.6;

    //
    // soft shadows
    //
    //vec2 c1 = v_texCoords;
    //vec4 col = texture2D(u_texture, c1);
    if( u_softShadows == 1 )
    {
        float blur = 0.004;
        float col_alpha = 0.0;

        col_alpha += texture2D(u_texture, c1 + vec2(4.0 * blur, -4.0 * blur)).a * 0.0162162162 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(3.0 * blur, -3.0 * blur)).a * 0.0540540541 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(2.0 * blur, -2.0 * blur)).a * 0.1216216216 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(1.0 * blur, -1.0 * blur)).a * 0.1945945946 / 2.0;

        col_alpha += texture2D(u_texture, c1 + vec2(-4.0 * blur)).a * 0.0162162162 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(-3.0 * blur)).a * 0.0540540541 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(-2.0 * blur)).a * 0.1216216216 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(-1.0 * blur)).a * 0.1945945946 / 2.0;

        col_alpha += col.a * 0.2270270270;

        col_alpha += texture2D(u_texture, c1 + vec2(1.0 * blur)).a * 0.1945945946 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(2.0 * blur)).a * 0.1216216216 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(3.0 * blur)).a * 0.0540540541 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(4.0 * blur)).a * 0.0162162162 / 2.0;

        col_alpha += texture2D(u_texture, c1 + vec2(-4.0 * blur, 4.0 * blur)).a * 0.0162162162 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(-3.0 * blur, 3.0 * blur)).a * 0.0540540541 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(-2.0 * blur, 2.0 * blur)).a * 0.1216216216 / 2.0;
        col_alpha += texture2D(u_texture, c1 + vec2(-1.0 * blur, 1.0 * blur)).a * 0.1945945946 / 2.0;

        col = vec4(col.rgb * col_alpha, 1.0);
    }
    else
    {
        col = vec4(col.rgb * col.a, 1.0);
    }

    gl_FragColor = col;
}
