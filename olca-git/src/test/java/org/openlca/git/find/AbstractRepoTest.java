package org.openlca.git.find;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openlca.util.Dirs;
import org.zeroturnaround.zip.ZipUtil;

public abstract class AbstractRepoTest {

	private static File tmpDir;
	protected static FileRepository repo;
	protected static final String[] commitIds = {
			"aba49d04179faa1034eaf6d221a903ef64f3dbaf",
			"63f8eeaf7e65f7817e604460053fa1dcbfb28d35",
			"0adbf8bddac2cae5c81801fd836075fc612e37e4",
			"079cffbd7fc044a18ae3be0a748c29537594b951",
			"db3ba75f99df098aec28726447ad583fea3bd93b",
			"0c9395b3c2e28a26265d35a146f64369c82085fe"
	};

	@BeforeClass
	public static void beforeClass() throws IOException, GitAPIException {
		tmpDir = Files.createTempDirectory("olca-cloud-test").toFile();
		ZipUtil.unpack(CommitsTest.class.getResourceAsStream("ref_data.zip"), tmpDir);
		repo = new FileRepository(tmpDir);
	}

	@AfterClass
	public static void afterClass() {
		repo.close();
		Dirs.delete(tmpDir.toPath());
	}

}
