#extension GL_OES_EGL_image_external : require

#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform samplerExternalOES u_texture;

void main() {
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
}
