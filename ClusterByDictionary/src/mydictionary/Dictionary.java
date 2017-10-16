package mydictionary;

public class Dictionary implements Comparable<Dictionary>{
	/**
	 * recordname 记录名称(疾病、药物) recordmodel 遗传模式 recordeffect 治疗疾病 rsid SNPID
	 * genename 基因 geno 基因型 catagrory 分类（有益，有害，正常） description 描述 reference 参考文献
	 * recordlabel 标签（疾病相关还是药物相关） diseaseCatagrory 疾病分类
	 */
	String recordname;
	String recordmodel;
	String recordeffect;
	String rsid;
	String genename;
	String geno;
	String catagrory;
	String description;
	String reference;
	String recordlabel;
	String diseaseCatagrory;

	public Dictionary() {
	}

	public Dictionary(String recordname, String recordmodel, String recordeffect, String rsid, String genename,
			String geno, String catagrory, String description, String reference, String recordlabel,
			String diseaseCatagrory) {
		super();
		this.recordname = recordname;
		this.recordmodel = recordmodel;
		this.recordeffect = recordeffect;
		this.rsid = rsid;
		this.genename = genename;
		this.geno = geno;
		this.catagrory = catagrory;
		this.description = description;
		this.reference = reference;
		this.recordlabel = recordlabel;
		this.diseaseCatagrory = diseaseCatagrory;
	}

	public String getRecordname() {
		return recordname;
	}

	public void setRecordname(String recordname) {
		this.recordname = recordname;
	}

	public String getRecordmodel() {
		return recordmodel;
	}

	public void setRecordmodel(String recordmodel) {
		this.recordmodel = recordmodel;
	}

	public String getRecordeffect() {
		return recordeffect;
	}

	public void setRecordeffect(String recordeffect) {
		this.recordeffect = recordeffect;
	}

	public String getRsid() {
		return rsid;
	}

	public void setRsid(String rsid) {
		this.rsid = rsid;
	}

	public String getGenename() {
		return genename;
	}

	public void setGenename(String genename) {
		this.genename = genename;
	}

	public String getGeno() {
		return geno;
	}

	public void setGeno(String geno) {
		this.geno = geno;
	}

	public String getCatagrory() {
		return catagrory;
	}

	public void setCatagrory(String catagrory) {
		this.catagrory = catagrory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getRecordlabel() {
		return recordlabel;
	}

	public void setRecordlabel(String recordlabel) {
		this.recordlabel = recordlabel;
	}

	public String getDiseaseCatagrory() {
		return diseaseCatagrory;
	}

	public void setDiseaseCatagrory(String diseaseCatagrory) {
		this.diseaseCatagrory = diseaseCatagrory;
	}

	@Override
	public String toString() {
		return "Dictionary [recordname=" + recordname + ", rsid=" + rsid + ", geno=" + geno + "]";
	}

	@Override
	public int compareTo(Dictionary o) {
		// TODO Auto-generated method stub
		return o.diseaseCatagrory.compareTo(this.diseaseCatagrory);
	}

}
