

class Coordinates {
	
	int x;
	int y;
	
	public Coordinates(int x, int y) {
		
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object object) {
		
		if(this == object) {
			return true;
		} if( object == null || getClass() != object.getClass()) {
			return false;
		}
		
		Coordinates other = (Coordinates) object;
		return  x == other.x && y == other.y;
		
	}
	

}
