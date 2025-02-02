#version 330
#define PI 3.14159265

in vec2 TexCoord;

uniform sampler2D texFront;
uniform sampler2D texRight;
uniform sampler2D texBack;
uniform sampler2D texLeft;
uniform sampler2D texBottom;
uniform sampler2D texTop;

out vec4 FragColor;

vec2 Remap (vec2 value, vec2 inMin, vec2 inMax, vec2 outMin, vec2 outMax){
    // 入力値を0から1の範囲に正規化
    vec2 base = clamp((value - inMin) / (inMax - inMin), 0.0, 1.0);
    // 目的の範囲に線形補間
    return outMin + (outMax - outMin) * base;
}

vec2 Mapping (vec2 Angle, vec2 inMin, vec2 inMax){
    return Remap(Angle, inMin, inMax, vec2(0.0, 0.0), vec2(1.0, 1.0));
}
vec3 genSphereVector(){
    vec2 source = -vec2(PI) + (TexCoord - vec2(0, 0)) * (vec2(PI) - -vec2(PI)) / (vec2(1, 1) - vec2(0, 0));
    float srcY = source.y / 2.0;
    float cosY = cos(srcY);
    return vec3(cosY * sin(source.x), sin(srcY), cosY * cos(source.x));
}
float genMask(vec3 SphereVector, vec3 DirectionVector, vec2 Map){
    float Mask_Direction = clamp(ceil(dot(SphereVector, DirectionVector)), 0.0, 1.0);
    vec2 Mask_Floor = 1.0 - floor(Map);
    vec2 Mask_Ceil = ceil(Map);
    return Mask_Floor.x *Mask_Floor.y *Mask_Ceil.x *Mask_Ceil.y * Mask_Direction;
}

vec3 genAngle(vec3 SphereVector){
    float dotYZ = dot(normalize(vec3(0.0, SphereVector.y, SphereVector.z)), SphereVector);
    float dotXY = dot(normalize(vec3(SphereVector.x, SphereVector.y, 0.0)), SphereVector);
    float dotXZ = dot(normalize(vec3(SphereVector.x, 0.0, SphereVector.z)), SphereVector);
    vec3 append = vec3(dotYZ, dotXZ, dotXY);
    return sqrt(1.0 - (append * append));
}

void main() {
    vec3 SphereVector = genSphereVector();
    vec3 Angle = genAngle(SphereVector);

    vec2 Angle_X = vec2((1.0 / Angle.x) * SphereVector.yz);
    vec2 Angle_Y = vec2((1.0 / Angle.y) * SphereVector.xz);
    vec2 Angle_Z = vec2((1.0 / Angle.z) * SphereVector.xy);

    vec2 Map_Left = Mapping(Angle_X, vec2(-1.0, -1.0), vec2(1.0, 1.0));
    vec2 Map_Right = Mapping(Angle_X, vec2(-1.0, 1.0), vec2(1.0, -1.0));
    vec2 Map_Bottom = Mapping(Angle_Y, vec2(-1.0, -1.0), vec2(1.0, 1.0));
    vec2 Map_Top = Mapping(Angle_Y, vec2(-1.0, 1.0), vec2(1.0, -1.0));
    vec2 Map_Back = Mapping(Angle_Z, vec2(1.0, -1.0), vec2(-1.0, 1.0));
    vec2 Map_Front = Mapping(Angle_Z, vec2(-1.0, -1.0), vec2(1.0, 1.0));

    vec4 Color_Left = texture(texLeft, vec2(Map_Left.y, Map_Left.x)) *genMask(SphereVector, vec3(-1.0, 0.0, 0.0), Map_Left);
    vec4 Color_Right = texture(texRight, vec2(Map_Right.y, Map_Right.x)) *genMask(SphereVector, vec3(1.0, 0.0, 0.0), Map_Right);
    vec4 Color_Bottom = texture(texBottom, Map_Bottom) *genMask(SphereVector, vec3(0.0, -1.0, 0.0), Map_Bottom);
    vec4 Color_Top = texture(texTop, Map_Top) *genMask(SphereVector, vec3(0.0, 1.0, 0.0), Map_Top);
    vec4 Color_Back = texture(texBack, Map_Back) *genMask(SphereVector, vec3(0.0, 0.0, -1.0), Map_Back);
    vec4 Color_Front = texture(texFront, Map_Front) *genMask(SphereVector, vec3(0.0, 0.0, 1.0), Map_Front);

    FragColor=Color_Left+Color_Right+Color_Back+Color_Front+Color_Bottom+Color_Top;
}