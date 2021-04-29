package spold2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * An EcoSpold 2 data set can contain an activity data set (aka process), a
 * child activity data set (an activity data set that extends another data set),
 * or even an LCIA method data set.
 */
@XmlRootElement(name = "ecoSpold")
public class EcoSpold2 {

	@XmlElement(name = "activityDataset")
	public DataSet dataSet;

	@XmlElement(name = "childActivityDataset")
	public DataSet childDataSet;

	@XmlElement(name = "impactMethod")
	public ImpactMethod impactMethod;

	public static EcoSpold2 read(File file) {
		try (FileInputStream stream = new FileInputStream(file)) {
			return read(stream);
		} catch (Exception e) {
			String m = "failed to read EcoSpold 2 file: " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static EcoSpold2 read(InputStream is) {
		try {
			return JAXB.unmarshal(is, EcoSpold2.class);
		} catch (Exception e) {
			String m = "failed to read EcoSpold 2 document";
			throw new RuntimeException(m, e);
		}
	}

	/** Writes an activity data set to an EcoSpold 02 file. */
	public static void write(DataSet dataSet, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			write(dataSet, fos);
		} catch (Exception e) {
			String m = "failed to write data set to file: " + file;
			throw new RuntimeException(m, e);
		}

	}

	/** Writes an activity data set to an EcoSpold 02 file. */
	public static void write(DataSet dataSet, OutputStream out) {
		try {
			EcoSpold2 spold2 = new EcoSpold2();
			spold2.dataSet = dataSet;
			JAXB.marshal(spold2, out);
		} catch (Exception e) {
			String m = "failed to write data set";
			throw new RuntimeException(m, e);
		}
	}

	/**
	 * Returns the activity data set (process) of the EcoSpold 2 file. Note
	 * that this can return null when the file contains an LCIA method and not
	 * an activity.
	 */
	public DataSet activity() {
		return dataSet != null ? dataSet : childDataSet;
	}

}
