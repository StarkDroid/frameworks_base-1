/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.testapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.android.scenegraph.*;
import com.android.scenegraph.SceneManager.SceneLoadedCallback;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.renderscript.*;
import android.renderscript.Program.TextureType;
import android.util.Log;

// This is where the scenegraph and the rendered objects are initialized and used
public class SimpleAppRS {

    private static String TAG = "SimpleAppRS";

    SceneManager mSceneManager;
    ArrayList<Renderable> geometry = new ArrayList<Renderable>();

    Scene mScene;
    RenderScriptGL mRS;
    Resources mRes;

    public void init(RenderScriptGL rs, Resources res, int width, int height) {
        mRS = rs;
        mRes = res;
        mSceneManager = SceneManager.getInstance();
        mSceneManager.initRS(mRS, mRes, width, height);

        mScene = new Scene();

        setupGeometry();
        setupCamera();
        setupRenderPass();
        setupShaders();

        mSceneManager.setActiveScene(mScene);

        mScene.initRS();
        mRS.bindRootScript(mSceneManager.getRenderLoop());
    }

    private void setupShaders() {
        // Built-in shader that provides position, texcoord and normal
        VertexShader genericV = SceneManager.getDefaultVS();
        // Built-in shader that displays a color
        FragmentShader colorF = SceneManager.getColorFS();
        mScene.assignRenderState(new RenderState(genericV, colorF, null, null));
    }
    private void setupGeometry() {
        Mesh.TriangleMeshBuilder tmb = new Mesh.TriangleMeshBuilder(mRS,
                                           3, Mesh.TriangleMeshBuilder.TEXTURE_0);

        tmb.setTexture(0.0f, 1.0f).addVertex(-1.0f, 1.0f, 0.0f);
        tmb.setTexture(0.0f, 0.0f).addVertex(-1.0f, -1.0f, 0.0f);
        tmb.setTexture(1.0f, 0.0f).addVertex(1.0f, -1.0f, 0.0f);
        tmb.setTexture(1.0f, 1.0f).addVertex(1.0f, 1.0f, 0.0f);

        tmb.addTriangle(0, 1, 2);
        tmb.addTriangle(2, 3, 0);

        Mesh mesh  = tmb.create(true);

        Renderable quad = new Renderable();
        quad.setMesh(mesh);
        quad.setTransform(new CompoundTransform());
        quad.appendSourceParams(new Float4Param("color", 0.2f, 0.3f, 0.4f));

        mScene.appendRenderable(quad);
        geometry.add(quad);
    }

    private void setupCamera() {
        Camera camera = new Camera();
        camera.setFar(200);
        camera.setNear(0.1f);
        camera.setFOV(60);
        CompoundTransform cameraTransform = new CompoundTransform();
        cameraTransform.addTranslate("camera", new Float3(0, 0, 10));
        mScene.appendTransform(cameraTransform);
        camera.setTransform(cameraTransform);
        mScene.appendCamera(camera);
    }

    private void setupRenderPass() {
        RenderPass mainPass = new RenderPass();
        mainPass.setClearColor(new Float4(1.0f, 1.0f, 1.0f, 1.0f));
        mainPass.setShouldClearColor(true);
        mainPass.setClearDepth(1.0f);
        mainPass.setShouldClearDepth(true);
        mainPass.setCamera(mScene.getCameras().get(0));
        for (Renderable renderable : geometry) {
            mainPass.appendRenderable(renderable);
        }
        mScene.appendRenderPass(mainPass);
    }
}
