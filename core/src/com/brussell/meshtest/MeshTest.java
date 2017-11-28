package com.brussell.meshtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;

public class MeshTest extends ApplicationAdapter implements RenderableProvider {
  private static final int NUM_QUADS_X_Y = 1000;
  private static final float JAGGEDNESS = 50f;
  public PerspectiveCamera cam;
  public ModelBatch modelBatch;

  private Mesh _mesh;
  private Material _mat;
  private FloatArray vertices = new FloatArray();

  private float r = NUM_QUADS_X_Y;
  private float theta = 0f;
  private float phi = 0f;

  @Override
  public void create() {
    modelBatch = new ModelBatch();

    cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.position.set(0f, 100f, 0f);
    cam.lookAt(0, 0, 0);
    cam.near = 1f;
    cam.far = 2000f;
    cam.update();

    initMaterial();
    initMesh();
    Gdx.input.setInputProcessor(new CameraInputController(cam));
  }

  private void initMaterial() {
    final Texture texture = new Texture(Gdx.files.internal("badlogic.jpg"));
    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    _mat = new Material(TextureAttribute.createDiffuse(texture));
  }

  private void initMesh() {
    for (int i = 0; i < NUM_QUADS_X_Y; i++) {
      for (int j = 0; j < NUM_QUADS_X_Y; j++) {
        addQuadVertices(i - NUM_QUADS_X_Y / 2, j - NUM_QUADS_X_Y / 2, 1f);
      }
    }
    _mesh = new Mesh(true, vertices.size, 0, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.TexCoords(0)));
    _mesh.setVertices(vertices.toArray());
  }

  private void addQuadVertices(final int x, final int z, final float scale) {
    addVertex(x * scale, MathUtils.random(JAGGEDNESS), z * scale);
    addVertex(x * scale, MathUtils.random(JAGGEDNESS), (z + 1) * scale);
    addVertex((x + 1) * scale, MathUtils.random(JAGGEDNESS), (z + 1) * scale);

    addVertex(x * scale, MathUtils.random(JAGGEDNESS), z * scale);
    addVertex((x + 1) * scale, MathUtils.random(JAGGEDNESS), (z + 1) * scale);
    addVertex((x + 1) * scale, MathUtils.random(JAGGEDNESS), z * scale);
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

    modelBatch.begin(cam);
    modelBatch.render(this);
    modelBatch.end();

    Quaternion q = new Quaternion(new Vector3(0.1f, 1f, 0.2f).nor(), JAGGEDNESS * Gdx.graphics.getDeltaTime());
    cam.rotate(q);
    theta += 0.1f * Gdx.graphics.getDeltaTime();
    phi += 0.010174f * Gdx.graphics.getDeltaTime();
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
    renderable.meshPart.size = _mesh.getNumVertices();
    renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
    renderable.worldTransform.setToTranslation(new Vector3());
    renderables.add(renderable);
  }
}
