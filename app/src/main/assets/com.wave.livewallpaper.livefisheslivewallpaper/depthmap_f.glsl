precision highp float;

varying vec4 v_position;

uniform float u_cameraFar;
uniform vec3 u_lightPosition;

void main()
{
	// Simple depth calculation, just the length of the vector light-current position
	gl_FragColor = vec4(distance(v_position.xyz, u_lightPosition) / u_cameraFar);
}

