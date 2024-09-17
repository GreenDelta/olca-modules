package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.AbstractRepositoryTests;

public class StashTests extends AbstractRepositoryTests {

	private void createDatabase() {
		repo.create("UNIT_GROUP/Technical unit groups/93a60a57-a4c8-11da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/258d6abd-14f2-4484-956c-c88e8f6fd8ed.json",
				"UNIT_GROUP/Technical unit groups/11d161f0-37e3-4d49-bf7a-ff4f31a9e5c7.json",
				"UNIT_GROUP/Economic unit groups/da299c4d-1741-4da8-9fbd-5ccfb5e1d688.json",
				"UNIT_GROUP/Technical unit groups/c288.json",
				"UNIT_GROUP/Technical unit groups/93a60a57-a3c8-18da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/f2275057-f8be-4db9-bb78-5dfc276967a0.json",
				"UNIT_GROUP/Technical unit groups/93a60a57-a3c8-20da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/36932b14-ba61-417b-a80c-eb9935d193f1.json",
				"UNIT_GROUP/Technical unit groups/93a60a57-a3c8-12da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/5beb6eed-33a9-47b8-9ede-1dfe8f679159.json",
				"UNIT_GROUP/Technical unit groups/93a60a57-a3c8-11da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/ff8ed45d-bbfb-4531-8c7b-9b95e52bd41d.json",
				"UNIT_GROUP/Technical unit groups/838aaa21-0117-11db-92e3-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/a6941776-ee4b-40fe-9a05-e19faac45240.json",
				"UNIT_GROUP/Technical unit groups/838aaa22-0117-11db-92e3-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/af638906-3ec7-4314-8de7-f76039f2dd01.json",
				"UNIT_GROUP/Technical unit groups/9e5a91be-b3d1-4268-8e7d-e5e93f6a75d4.json",
				"UNIT_GROUP/Technical unit groups/5454b231-270e-45e6-89b2-7f4f3e482245.json",
				"UNIT_GROUP/Technical unit groups/59f6a0a2-731f-41c3-86df-d383dc673dfe.json",
				"UNIT_GROUP/Technical unit groups/93a60a57-a3c8-16da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/3dbb60e1-edde-49f7-b28d-f34b4af727b3.json",
				"UNIT_GROUP/Technical unit groups/af16ae7e-3e04-408a-b8ae-5b3666dbe7f9.json",
				"UNIT_GROUP/Technical unit groups/93a60a57-a3c8-23da-a746-0800200c9a66.json",
				"UNIT_GROUP/Technical unit groups/326eb58b-e5b3-4cea-b45a-2398c25109f8.json",
				"UNIT_GROUP/Technical unit groups/876adcd3-29e6-44e2-acdd-11be304ae654.json",
				"UNIT_GROUP/Technical unit groups/59f191d6-5dd3-4553-af88-1a32accfe308.json",
				"FLOW_PROPERTY/Technical flow properties/ac95ed26-5038-4862-8b82-94f1412875cd.json",
				"FLOW_PROPERTY/Technical flow properties/27f62f94-3fe1-4df5-9693-9112b832decb.json",
				"FLOW_PROPERTY/Economic flow properties/fdfecf14-ff8a-4e17-b2b2-f938c4b5cc27.json",
				"FLOW_PROPERTY/Technical flow properties/c0447923-0e60-4b3c-97c2-a86dddd9eea5.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-17da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-14da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/58ea2de8-1f31-4248-9b03-18ec5d8db13b.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-11da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/838aaa20-0117-11db-92e3-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/2d9d802c-002d-4018-a702-d2d46e78625a.json",
				"FLOW_PROPERTY/Technical flow properties/441238a3-ba09-46ec-b35b-c30cfba746d1.json",
				"FLOW_PROPERTY/Technical flow properties/e2f7001e-a331-4fc7-8052-c0b9bcf6a05f.json",
				"FLOW_PROPERTY/Technical flow properties/4e10f566-0358-489a-8e3a-d687b66c50e6.json",
				"FLOW_PROPERTY/Technical flow properties/a819760a-7651-4579-a786-842f7575df60.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-21da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/45915a2c-6ee8-45bd-8a46-070a7261558e.json",
				"FLOW_PROPERTY/Technical flow properties/64771383-534c-4051-84ca-e195564d5425.json",
				"FLOW_PROPERTY/Technical flow properties/ecbe0a5d-f397-4b74-993d-32d231e4bcf9.json",
				"FLOW_PROPERTY/Technical flow properties/8766e10f-9f41-4db0-8173-ad0d002a5b98.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-11da-a746-0800200b9a66.json",
				"FLOW_PROPERTY/Technical flow properties/8a9107c2-62a2-4997-95b0-6944c80b774e.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-19da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-22da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/e0d963f9-d6a4-42a5-90e9-fff8452aa2af.json",
				"FLOW_PROPERTY/Technical flow properties/f6811440-ee37-11de-8a39-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/01846770-4cfe-4a25-8ad9-919d8d378345.json",
				"FLOW_PROPERTY/Technical flow properties/0ddc622a-bc4a-4cf8-a551-9e112864b77f.json",
				"FLOW_PROPERTY/Technical flow properties/c6984745-192d-416f-9728-d6169ba6267f.json",
				"FLOW_PROPERTY/Technical flow properties/fd9a098b-253e-4f1e-986d-76a775c51722.json",
				"FLOW_PROPERTY/Technical flow properties/93a60a56-a3c8-13da-a746-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/b7ffb330-95a8-4815-a78c-47fcdee3b768.json",
				"FLOW_PROPERTY/Technical flow properties/838aaa23-0117-11db-92e3-0800200c9a66.json",
				"FLOW_PROPERTY/Technical flow properties/05e42ba1-dbcd-4dc2-b735-a2f17dd211ae.json",
				"CURRENCY/505a.08FF.json",
				"CURRENCY/a66b1ada-8042-44e9-88ac-6c89d2dc8e06.json",
				"CURRENCY/1ab0ca1b-79e7-4d5b-b501-cbf043ff302d.json",
				"CURRENCY/30abeb04-b361-4ff3-8f63-b36796da7cb2.json",
				"CURRENCY/7de74e65-ff97-404f-bf57-5d2a9134fb43.json",
				"CURRENCY/7ddb41a4-823d-4432-bf6c-3ea8604b41be.json",
				"CURRENCY/3eee2da0-75ae-4f84-972c-e15e58167ef2.json",
				"CURRENCY/2f1a6e69-442b-41bd-91dc-cb9e2e4e75c0.json",
				"CURRENCY/24217f9a-0f63-4dc8-8440-c5829145f263.json",
				"CURRENCY/0b705d37-d71c-4c8f-8e02-2b36663635c6.json",
				"CURRENCY/b24f78ff-576b-440e-a12b-649374520fa0.json",
				"CURRENCY/d780891e-1d81-4d25-9912-33869823a220.json");
	}

	@Test
	public void testStashCreate() throws GitAPIException, IOException {
		createDatabase();
		var diffs = repo.diffs.find().withDatabase();
		Assert.assertEquals(76, diffs.size());
		repo.stashWorkspace();
		Assert.assertEquals(0, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(0, repo.count(ModelType.CURRENCY));
		Assert.assertEquals(0, repo.count(ModelType.UNIT_GROUP));
		Assert.assertEquals(0, repo.count(ModelType.FLOW_PROPERTY));
		Assert.assertEquals(0, repo.diffs.find().withDatabase().size());
	}

	@Test
	public void testStashApply() throws IOException, GitAPIException {
		createDatabase();
		repo.stashWorkspace();
		Assert.assertNotNull(repo.commits.stash());
		GitStashApply.on(repo).run();
		Assert.assertNull(repo.commits.stash());
		var diffs = repo.diffs.find().withDatabase();
		Assert.assertEquals(76, diffs.size());
		Assert.assertEquals(4, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(12, repo.count(ModelType.CURRENCY));
		Assert.assertEquals(27, repo.count(ModelType.UNIT_GROUP));
		Assert.assertEquals(33, repo.count(ModelType.FLOW_PROPERTY));
	}

	@Test
	public void testStashApplySameCategory() throws IOException, GitAPIException {
		repo.create("ACTOR/test/505a.08FF.json");
		Assert.assertEquals(1, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(1, repo.count(ModelType.ACTOR));
		repo.stashWorkspace();
		Assert.assertEquals(0, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(0, repo.count(ModelType.ACTOR));
		repo.create("ACTOR/test/605a07ff-16d7-4a83-b131-66998dad1732.json");
		GitStashApply.on(repo).run();
		Assert.assertEquals(1, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(2, repo.count(ModelType.ACTOR));
	}

	@Test
	public void testStashApplyKeepDeletedCategory() throws IOException, GitAPIException {
		repo.create("ACTOR/test/505a.08FF.json");
		Assert.assertEquals(1, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(1, repo.count(ModelType.ACTOR));
		repo.commitWorkspace();
		repo.delete("ACTOR/test/505a.08FF.json",
				"ACTOR/test");
		Assert.assertEquals(0, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(0, repo.count(ModelType.ACTOR));
		repo.stashWorkspace();
		Assert.assertEquals(1, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(1, repo.count(ModelType.ACTOR));
		repo.create("ACTOR/test/605a07ff-16d7-4a83-b131-66998dad1732.json");
		Assert.assertEquals(2, repo.count(ModelType.ACTOR));
		GitStashApply.on(repo).run();
		Assert.assertEquals(1, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(1, repo.count(ModelType.ACTOR));
	}

	@Test
	public void testStashDrop() throws IOException, GitAPIException {
		createDatabase();
		repo.stashWorkspace();
		GitStashDrop.from(repo).run();
		Assert.assertNull(repo.commits.stash());
	}

	@Test
	public void testDiscard() throws GitAPIException, IOException {
		repo.create("ACTOR/test/505a.08FF.json");
		Assert.assertEquals(1, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(1, repo.count(ModelType.ACTOR));
		repo.stashWorkspace();
		Assert.assertEquals(0, repo.commits.find().all().size());
		Assert.assertEquals(0, repo.diffs.find().withDatabase().size());
		Assert.assertEquals(0, repo.count(ModelType.CATEGORY));
		Assert.assertEquals(0, repo.count(ModelType.ACTOR));
	}

}
