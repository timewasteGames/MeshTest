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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;

public class MeshTest extends ApplicationAdapter implements RenderableProvider {
  public PerspectiveCamera cam;
  public ModelBatch modelBatch;

  private Mesh _mesh;
  private Material _mat;
  private FloatArray vertices = new FloatArray();

  @Override
  public void create() {
    modelBatch = new ModelBatch();

    cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.position.set(0f, 10f, -10f);
    cam.lookAt(0, 0, 0);
    cam.near = 1f;
    cam.far = 1000f;
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
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 20; j++) {
        addQuadVertices(i - 10, j, 20f);
      }
    }
    _mesh = new Mesh(true, vertices.size, 0, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.TexCoords(0)));
    _mesh.setVertices(vertices.toArray());
  }

  private void addQuadVertices(final int x, final int z, final float scale) {
    addVertex(x * scale, 0f, z * scale);
    addVertex(x * scale, 0f, (z + 1) * scale);
    addVertex((x + 1) * scale, 0f, (z + 1) * scale);

    addVertex(x * scale, 0f, z * scale);
    addVertex((x + 1) * scale, 0f, (z + 1) * scale);
    addVertex((x + 1) * scale, 0f, z * scale);
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
