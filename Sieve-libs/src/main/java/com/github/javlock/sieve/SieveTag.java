package com.github.javlock.sieve;

import java.util.ArrayList;
import java.util.Objects;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "pdfTags")
public class SieveTag {

	@DatabaseField(id = true, canBeNull = false, unique = true)
	private @Getter @Setter String tag;

	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private @Getter ArrayList<String> pdfFilesIds = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SieveTag [tag=");
		builder.append(tag);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SieveTag other = (SieveTag) obj;
		return Objects.equals(tag, other.tag);
	}

}
