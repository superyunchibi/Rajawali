/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.materials;

import rajawali.lights.ALight;
import android.graphics.Color;
import android.opengl.GLES20;

import com.monyetmabuk.livewallpapers.photosdof.R;


public class GouraudMaterial extends AAdvancedMaterial {
	
	protected int muSpecularColorHandle;
	protected int muSpecularIntensityHandle;
	protected float[] mSpecularColor;
	protected float[] mSpecularIntensity;
	
	public GouraudMaterial() {
		super(R.raw.gouraud_material_vertex, R.raw.gouraud_material_fragment);
		mSpecularColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		mSpecularIntensity = new float[] { 1f, 1f, 1f, 1.0f };
	}

	public GouraudMaterial(float[] specularColor) {
		this();
		mSpecularColor = specularColor;
	}

	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform4fv(muSpecularIntensityHandle, 1, mSpecularIntensity, 0);
	}
	
	public void setSpecularColor(float[] color) {
		mSpecularColor = color;
	}
	
	public void setSpecularColor(float r, float g, float b, float a) {
		mSpecularColor[0] = r;
		mSpecularColor[1] = g;
		mSpecularColor[2] = b;
		mSpecularColor[3] = a;
	}
	
	public void setSpecularColor(int color) {
		setSpecularColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f);
	}
	
	public void setSpecularIntensity(float[] intensity) {
		mSpecularIntensity = intensity;
	}
	
	public void setSpecularIntensity(float r, float g, float b, float a) {
		mSpecularIntensity[0] = r;
		mSpecularIntensity[1] = g;
		mSpecularIntensity[2] = b;
		mSpecularIntensity[3] = a;
	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
		StringBuffer vc = new StringBuffer();
		vc.append("float power = 0.0;\n");

		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);

			if(light.getLightType() == ALight.POINT_LIGHT) {
				vc.append("L = normalize(uLightPosition").append(i).append(" + E);\n");
				vc.append("dist = distance(-E, uLightPosition").append(i).append(");\n");
				vc.append("attenuation = 1.0 / (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.SPOT_LIGHT) {
				vc.append("dist = distance(-E, uLightPosition").append(i).append(");\n");
				vc.append("attenuation = (uLightAttenuation").append(i).append("[1] + uLightAttenuation").append(i).append("[2] * dist + uLightAttenuation").append(i).append("[3] * dist * dist);\n");
				vc.append("L = normalize(uLightPosition").append(i).append(" + E);\n");
				vc.append("vec3 spotDir").append(i).append(" = normalize(-uLightDirection").append(i).append(");\n");
				vc.append("float spot_factor").append(i).append(" = dot( L, spotDir").append(i).append(" );\n");
				vc.append("if( uSpotCutoffAngle").append(i).append(" < 180.0 ) {\n");
					vc.append("if( spot_factor").append(i).append(" >= cos( radians( uSpotCutoffAngle").append(i).append(") ) ) {\n");
						vc.append("spot_factor").append(i).append(" = (1.0 - (1.0 - spot_factor").append(i).append(") * 1.0/(1.0 - cos( radians( uSpotCutoffAngle").append(i).append("))));\n");
						vc.append("spot_factor").append(i).append(" = pow(spot_factor").append(i).append(", uSpotFalloff").append(i).append("* 1.0/spot_factor").append(i).append(");\n");
					vc.append("}\n");
					vc.append("else {\n");
						vc.append("spot_factor").append(i).append(" = 0.0;\n");
					vc.append("}\n");
					vc.append("L = vec3(L.x, L.y, L.z) * spot_factor").append(i).append(";\n");
					vc.append("}\n");
			}  else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("L = normalize(-uLightDirection").append(i).append(");");
			}
			vc.append("NdotL = max(dot(N, L), 0.1);\n");
			vc.append("power = NdotL * attenuation * uLightPower").append(i).append(";\n");
			vc.append("vDiffuseIntensity += power;\n");
			vc.append("vLightColor += power * uLightColor").append(i).append(";\n");
			vc.append("vSpecularIntensity += pow(NdotL, 6.0) * attenuation * uLightPower").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader);
		muSpecularColorHandle = getUniformLocation("uSpecularColor");
		muSpecularIntensityHandle = getUniformLocation("uSpecularIntensity");
	}
}