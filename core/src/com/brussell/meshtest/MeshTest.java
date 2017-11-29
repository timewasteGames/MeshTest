package com.brussell.meshtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;

public class MeshTest extends ApplicationAdapter implements RenderableProvider {
  private static final int NUM_QUADS_X_Y = 1500;
  private static final int NUM_FLOATS_TO_DEFINE_VERTEX = 5;
  private static final float LUMPYNESS = 50f;
  private static final float SCALE = 1f;

  public PerspectiveCamera cam;
  public ModelBatch modelBatch;

  private Mesh _mesh;
  private Material _mat;
  private FloatArray vertices = new FloatArray();
  private ShortArray indices = new ShortArray();

  private float r = NUM_QUADS_X_Y * SCALE;
  private float theta = 0f;
  private float phi = 0f;

  private boolean INDEX_VERTICES = false;

  @Override
  public void create() {
    modelBatch = new ModelBatch();

    cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.near = 1f;
    cam.far = 5000f;

    initMaterial();
    initMesh();
    Gdx.input.setInputProcessor(new InputMultiplexer(new CameraInputController(cam), new InputAdapter() {
      @Override
      public boolean keyUp(final int keycode) {
        _mesh.dispose();
        vertices.clear();
        indices.clear();
        initMesh();
        return true;
      }
    }));
  }

  private void initMesh() {
    if (INDEX_VERTICES) {
      initMeshVerticesAndIndices();
    }
    else {
      initMeshVerticesOnly();
    }
  }

  private void initMaterial() {
    final Texture texture = new Texture(Gdx.files.internal("badlogic.jpg"));
    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    _mat = new Material(TextureAttribute.createDiffuse(texture));
    _mat.set(IntAttribute.createCullFace(1));
  }

  private void initMeshVerticesAndIndices() {
    for (int i = 0; i < NUM_QUADS_X_Y; i++) {
      for (int j = 0; j < NUM_QUADS_X_Y; j++) {
        addIndexedQuadVertices(i - NUM_QUADS_X_Y / 2, j - NUM_QUADS_X_Y / 2, SCALE);
      }
    }
    _mesh = new Mesh(true, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
    _mesh.setVertices(vertices.toArray());
    _mesh.setIndices(indices.toArray());
  }

  private void addIndexedQuadVertices(final int x, final int z, final float scale) {
    indices.add(addIndexedVertex(x * scale, MathUtils.sinDeg(z) * LUMPYNESS, z * scale));
    indices.add(addIndexedVertex(x * scale, MathUtils.sinDeg(z + 1) * LUMPYNESS, (z + 1) * scale));
    indices.add(addIndexedVertex((x + 1) * scale, MathUtils.sinDeg(z + 1) * LUMPYNESS, (z + 1) * scale));

    indices.add(addIndexedVertex(x * scale, MathUtils.sinDeg(z) * LUMPYNESS, z * scale));
    indices.add(addIndexedVertex((x + 1) * scale, MathUtils.sinDeg(z + 1) * LUMPYNESS, (z + 1) * scale));
    indices.add(addIndexedVertex((x + 1) * scale, MathUtils.sinDeg(z) * LUMPYNESS, z * scale));
  }

  private short addIndexedVertex(final float x, final float y, final float z) {
    // Search the existing vertices (backwards, since it is more likely that they were just added).
    for (int verticesIdx = vertices.size - NUM_FLOATS_TO_DEFINE_VERTEX; verticesIdx >= 0; verticesIdx -= NUM_FLOATS_TO_DEFINE_VERTEX) {
      if (MathUtils.isEqual(vertices.get(verticesIdx), x)
          && MathUtils.isEqual(vertices.get(verticesIdx + 1), y)
          && MathUtils.isEqual(vertices.get(verticesIdx + 2), z)) {
        return (short) (verticesIdx / NUM_FLOATS_TO_DEFINE_VERTEX);
      }
    }
    // Add new one.
    addVertex(x, y, z);
    return (short) (vertices.size / NUM_FLOATS_TO_DEFINE_VERTEX - 1);
  }

  private void initMeshVerticesOnly() {
    for (int i = 0; i < NUM_QUADS_X_Y; i++) {
      for (int j = 0; j < NUM_QUADS_X_Y; j++) {
        addQuadVertices(i - NUM_QUADS_X_Y / 2, j - NUM_QUADS_X_Y / 2, SCALE);
      }
    }
    _mesh = new Mesh(true, vertices.size, 0, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.TexCoords(0)));
    _mesh.setVertices(vertices.toArray());
  }

  private void addQuadVertices(final int x, final int z, final float scale) {
    addVertex(x * scale, MathUtils.sinDeg(z) * LUMPYNESS, z * scale);
    addVertex(x * scale, MathUtils.sinDeg(z + 1) * LUMPYNESS, (z + 1) * scale);
    addVertex((x + 1) * scale, MathUtils.sinDeg(z + 1) * LUMPYNESS, (z + 1) * scale);

    addVertex(x * scale, MathUtils.sinDeg(z) * LUMPYNESS, z * scale);
    addVertex((x + 1) * scale, MathUtils.sinDeg(z + 1) * LUMPYNESS, (z + 1) * scale);
    addVertex((x + 1) * scale, MathUtils.sinDeg(z) * LUMPYNESS, z * scale);
  }

  private void addVertex(final float x, final float y, final float z) {
    // Position
    vertices.add(x);
    vertices.add(y);
    vertices.add(z);

    // UV
    vertices.add(x / 200f);
    vertices.add(z / 200f);
  }

  @Override
  public void render() {
    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    updateCam();

    modelBatch.begin(cam);
    modelBatch.render(this);
    modelBatch.end();
  }

  private void updateCam() {
    theta += 0.5f * Gdx.graphics.getDeltaTime();
    phi += 0.050174f * Gdx.graphics.getDeltaTime();
    cam.position.set(
        r * MathUtils.cos(theta) * MathUtils.cos(phi),
        r * MathUtils.cos(theta) * MathUtils.sin(phi),
        r * MathUtils.sin(theta));
    cam.lookAt(0f, 0f, 0f);
    cam.update();
  }

  @Override
  public void dispose() {
    modelBatch.dispose();
  }

  @Override
  public void getRenderables(final Array<Renderable> renderables, final Pool<Renderable> pool) {
    Renderable renderable = pool.obtain();
    renderable.material = _mat;
    renderable.meshPart.mesh = _mesh;
    renderable.meshPart.offset = 0;
    if (INDEX_VERTICES) {
      renderable.meshPart.size = _mesh.getNumIndices();
    }
    else {
      renderable.meshPart.size = _mesh.getNumVertices();
    }
    renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
    renderable.worldTransform.setToTranslation(new Vector3());
    renderables.add(renderable);
  }
}
