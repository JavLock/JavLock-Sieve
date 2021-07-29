package aaa;

import java.io.Serializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "pdfFiles")
public class PdfFile implements Serializable {
	private static final long serialVersionUID = 2983984940303647075L;

	@DatabaseField(id = true, canBeNull = false, unique = true)
	private @Getter @Setter String id;
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] data;

}
