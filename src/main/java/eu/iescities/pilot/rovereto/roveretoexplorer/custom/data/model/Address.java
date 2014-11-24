package eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model;

public class Address {
	private String luogo = null;
	private String via = null;
	private String citta = null;

	public String getLuogo() {
		return luogo;
	}

	public void setLuogo(String luogo) {
		this.luogo = luogo;
	}

	public String getVia() {
		return via;
	}

	public void setVia(String via) {
		this.via = via;
	}

	public String getCitta() {
		return citta;
	}

	public void setCitta(String citta) {
		this.citta = citta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((citta == null) ? 0 : citta.hashCode());
		result = prime * result + ((luogo == null) ? 0 : luogo.hashCode());
		result = prime * result + ((via == null) ? 0 : via.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (citta == null) {
			if (other.citta != null)
				return false;
		} else if (!citta.equals(other.citta))
			return false;
		if (luogo == null) {
			if (other.luogo != null)
				return false;
		} else if (!luogo.equals(other.luogo))
			return false;
		if (via == null) {
			if (other.via != null)
				return false;
		} else if (!via.equals(other.via))
			return false;
		return true;
	}

}
