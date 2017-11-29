package com.brussell.meshtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.FPSLogger;
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
  private static final int NUM_CHUNKS_X_Y = 50;
  private static final int NUM_QUADS_X_Y = 50;
  private static final int NUM_FLOATS_TO_DEFINE_VERTEX = 5;
  private static final float LUMPINESS = 50f;
  private static final float SCALE = 1f;

  private static final FPSLogger FPS_LOGGER = new FPSLogger();

  public PerspectiveCamera _cam;
  public ModelBatch _modelBatch;

  private Array<Mesh> _meshes = new Array<Mesh>();
  private Material _mat;
  private FloatArray vertices = new FloatArray();
  private ShortArray indices = new ShortArray();

  private float _r = NUM_CHUNKS_X_Y * NUM_QUADS_X_Y * SCALE;
  private float _theta = 0f;
  private float _phi = 0f;

  private boolean INDEX_VERTICES = false;

  @Override
  public void create() {
    _modelBatch = new ModelBatch();

    _cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    _cam.near = 1f;
    _cam.far = 5000f;

    initMaterial();
    initMeshes();
    Gdx.input.setInputProcessor(new InputMultiplexer(new CameraInputController(_cam), new InputAdapter() {
      @Override
      public boolean keyUp(final int keycode) {
        for (Mesh mesh : _meshes) {
          mesh.dispose();
        }
        initMeshes();
        return true;
      }
    }));
  }

  private void initMaterial() {
    final Texture texture = new Texture(Gdx.files.internal("badlogic.jpg"));
    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    _mat = new Material(TextureAttribute.createDiffuse(texture));
    _mat.set(IntAttribute.createCullFace(1));
  }

  private void initMeshes() {
    _meshes.clear();
    for (int i = 0; i < NUM_CHUNKS_X_Y; i++) {
      for (int j = 0; j < NUM_CHUNKS_X_Y; j++) {
        if (INDEX_VERTICES) {
          _meshes.add(initMeshVerticesAndIndices(i - NUM_CHUNKS_X_Y / 2, j - NUM_CHUNKS_X_Y / 2));
        }
        else {
          _meshes.add(initMeshVerticesOnly(i - NUM_CHUNKS_X_Y / 2, j - NUM_CHUNKS_X_Y / 2));
        }
        vertices.clear();
        indices.clear();
      }
    }
  }

  private Mesh initMeshVerticesAndIndices(final int xChunk, final int zChunk) {
    for (int i = 0; i < NUM_QUADS_X_Y; i++) {
      for (int j = 0; j < NUM_QUADS_X_Y; j++) {
        addIndexedQuadVertices(i - NUM_QUADS_X_Y / 2, j - NUM_QUADS_X_Y / 2, xChunk, zChunk);
      }
    }
    Mesh mesh = new Mesh(true, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
    mesh.setVertices(vertices.toArray());
    mesh.setIndices(indices.toArray());
    return mesh;
  }

  private void addIndexedQuadVertices(final int x, final int z, final int xChunk, final int zChunk) {
    float xOffset = xChunk * NUM_QUADS_X_Y * SCALE;
    float zOffset = zChunk * NUM_QUADS_X_Y * SCALE;

    float xLow = x * SCALE - xOffset;
    float xHigh = (x + 1) * SCALE - xOffset;
    float yLow = MathUtils.sinDeg(z - zChunk * NUM_QUADS_X_Y) * LUMPINESS;
    float yHigh = MathUtils.sinDeg(z + 1 - zChunk * NUM_QUADS_X_Y) * LUMPINESS;
    float zLow = z * SCALE - zOffset;
    float zHigh = (z + 1) * SCALE - zOffset;

    indices.add(addIndexedVertex(xLow, yLow, zLow));
    indices.add(addIndexedVertex(xLow, yHigh, zHigh));
    indices.add(addIndexedVertex(xHigh, yHigh, zHigh));

    indices.add(addIndexedVertex(xLow, yLow, zLow));
    indices.add(addIndexedVertex(xHigh, yHigh, zHigh));
    indices.add(addIndexedVertex(xHigh, yLow, zLow));
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

  private Mesh initMeshVerticesOnly(final int xChunk, final int zChunk) {
    for (int i = 0; i < NUM_QUADS_X_Y; i++) {
      for (int j = 0; j < NUM_QUADS_X_Y; j++) {
        addQuadVertices(i - NUM_QUADS_X_Y / 2, j - NUM_QUADS_X_Y / 2, xChunk, zChunk);
      }
    }
    Mesh mesh = new Mesh(true, vertices.size, 0, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.TexCoords(0)));
    mesh.setVertices(vertices.toArray());
    return mesh;
  }

  private void addQuadVertices(final int x, final int z, final int xChunk, final int zChunk) {
    float xOffset = xChunk * NUM_QUADS_X_Y * SCALE;
    float zOffset = zChunk * NUM_QUADS_X_Y * SCALE;

    float xLow = x * SCALE - xOffset;
    float xHigh = (x + 1) * SCALE - xOffset;
    float yLow = MathUtils.sinDeg(z - zChunk * NUM_QUADS_X_Y) * LUMPINESS;
    float yHigh = MathUtils.sinDeg(z + 1 - zChunk * NUM_QUADS_X_Y) * LUMPINESS;
    float zLow = z * SCALE - zOffset;
    float zHigh = (z + 1) * SCALE - zOffset;

    addVertex(xLow, yLow, zLow);
    addVertex(xLow, yHigh, zHigh);
    addVertex(xHigh, yHigh, zHigh);

    addVertex(xLow, yLow, zLow);
    addVertex(xHigh, yHigh, zHigh);
    addVertex(xHigh, yLow, zLow);
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

    _modelBatch.begin(_cam);
    _modelBatch.render(this);
    _modelBatch.end();

    FPS_LOGGER.log();
  }

  private void updateCam() {
    _theta += 0.5f * Gdx.graphics.getDeltaTime();
    _phi += 0.050174f * Gdx.graphics.getDeltaTime();
    _cam.position.set(
        _r * MathUtils.cos(_theta) * MathUtils.cos(_phi),
        _r * MathUtils.cos(_theta) * MathUtils.sin(_phi),
        _r * MathUtils.sin(_theta));
    _cam.lookAt(0f, 0f, 0f);
    _cam.update();
  }

  @Override
  public void dispose() {
    _modelBatch.dispose();
  }

  @Override
  public void getRenderables(final Array<Renderable> renderables, final Pool<Renderable> pool) {
    for (Mesh mesh : _meshes) {
      Renderable renderable = pool.obtain();
      renderable.material = _mat;
      renderable.meshPart.mesh = mesh;
      renderable.meshPart.offset = 0;
      if (INDEX_VERTICES) {
        renderable.meshPart.size = mesh.getNumIndices();
      }
      else {
        renderable.meshPart.size = mesh.getNumVertices();
      }
      renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
      renderable.worldTransform.setToTranslation(new Vector3());
      renderables.add(renderable);
    }
  }
}
