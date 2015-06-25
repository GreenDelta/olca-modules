package org.openlca.core.matrix.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;

/**
 * A class for reading matrices in the Matrix Market format 
 * (http://math.nist.gov/MatrixMarket/formats.html) This reader currently 
 * supports the coordinate and array format for matrices with floating point 
 * numbers.
 */
public class MarketFormatReader {

	private IMatrixFactory<?> factory;
	
	public MarketFormatReader(IMatrixFactory<?> factory) {
		this.factory = factory;
	}
	
	public IMatrix read(File file) throws IOException {
		try(Reader r = new FileReader(file);
				BufferedReader buff = new BufferedReader(r)){
			String header = buff.readLine();
		} 
		return null;
	}
	
}
