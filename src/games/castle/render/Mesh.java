package games.castle.render;

public class Mesh {
	
	private final int vaoId, numVertices;
	
	public Mesh(int vaoId, int numVertices) {
		this.vaoId = vaoId;
		this.numVertices = numVertices;
	}
	
	public int getVaoId() { return vaoId; }
	
	public int getNumVertices() { return numVertices; }
}