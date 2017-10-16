package mydictionary;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class ClusterByDictionary {
	public static void main(String[] args) {
		/**
		 * ar0:输入路径
		 * ar1:输出路径
		 * ar2:参考序列路径（在rawdata文件夹下/rawdata/dictionary）
		 */
		String ar0 = "Z:\\microarray\\bioinfo-DNA\\project\\jingdian_PMRA\\formalRelease-3-补\\分型结果";//"Z:\\microarray\\bioinfo-DNA\\project\\201708\\jingdian_PMRA\\formalRelease-2\\分型结果";//args[0]  Z:\\microarray\\bioinfo-DNA\\project\\201708\\jingdian_PMRA\\formalRelease-1\\分型结果
		String ar1 = "Z:\\microarray\\bioinfo-DNA\\project\\jingdian_PMRA\\formalRelease-3-补\\pdf\\";//"Z:\\microarray\\bioinfo-DNA\\project\\201708\\jingdian_PMRA\\formalRelease-2\\pdf\\";//"D:\\pdf\\1\\";//args[1]  D:\\pdf\\2\\
		String ar2 = "C:\\Users\\Administrator\\Desktop\\test\\dictionary";//"C:\\Users\\czp\\Desktop\\dictionary";//args[2]  C:\\Users\\czp\\Desktop\\dictionary
		ClusterByDictionary clusterByDictionary = new ClusterByDictionary();
		ArrayList<Dictionary> dictionaryList = clusterByDictionary.createDictionaryList(ar2);
		System.out.println(dictionaryList.size());
		final String regex = "^[AGTC]+/[-]+|^[-]+/[AGTC]+";
		final Pattern p = Pattern.compile("\\W+");
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
		long startTime = System.currentTimeMillis();
		String filePath = ar0;
		if (null == filePath || "".equals(filePath)) {
			return;
		}

		try {
			HashMap<String, String> map = clusterByDictionary.readfile(filePath);
			for (Entry<String, String> entry : map.entrySet()) {
				fixedThreadPool.execute(new Runnable() {
					public void run() {
						try {
							Scanner input = new Scanner(new File(entry.getKey()));
							String result = "";
							List diseaseList = new ArrayList<Dictionary>();
							List drugList = new ArrayList<Dictionary>();
							HashMap resultMap = new LinkedHashMap();
							System.out.println(Thread.currentThread().getName() + "---" + entry.getValue());
							while (input.hasNextLine()) {
								result = input.nextLine();
								String[] strArray = result.split("\t");
								if (null == strArray || "".equals(strArray)) {
									return;
								}
								// 获取第一列数据rsid
								String rsid = "";
								// 获取第二列数据基因型
								String geno = "";
								try {
									rsid = strArray[0].trim();
									geno = strArray[1].trim();
									for (Dictionary dictionary : dictionaryList) {
										if (dictionary.rsid.equals(rsid) && dictionary.geno.equals(geno)) {
											if (dictionary.recordlabel.equals("GENETIC_DISEASE")) {
												diseaseList.add(dictionary);
											} else {
												drugList.add(dictionary);
											}
										}/* else if (dictionary.rsid.equals(rsid) && dictionary.geno.matches(regex)
												&& geno.matches(regex)) {
											String[] m = p.split(dictionary.geno);
											String[] m1 = p.split(geno);
											String n = m.toString().replace(",", "").replace("[", "").replace("]", "")
													.trim();
											String n1 = m1.toString().replace(",", "").replace("[", "").replace("]", "")
													.trim();
											if (n.equals(n1)) {
												if (dictionary.recordlabel.equals("GENETIC_DISEASE")) {
													diseaseList.add(dictionary);
												} else {
													drugList.add(dictionary);
												}
											}
										}*/else if(dictionary.rsid.equals(rsid)){
											String[] stringArray = geno.split("\\/");
											String[] stringArray1 = dictionary.geno.split("\\/");
											if(stringArray.length==2 && stringArray1.length==2){
												if(stringArray1[1].equals(stringArray[0]) && stringArray1[0].equals(stringArray[1])){
													if (dictionary.recordlabel.equals("GENETIC_DISEASE")) {
														diseaseList.add(dictionary);
													} else {
														drugList.add(dictionary);
													}
												}
											}
										}
									}
								} catch (Exception e) {
									System.out.println("请注意：" + entry.getValue() + "出错了");
									e.printStackTrace();
								}
							}
							resultMap.put("GENETIC_DISEASE", diseaseList);
							resultMap.put("MEDICINE_RELATION", drugList);
							resultMap.put("workerid", entry.getValue());

							String outFilePath = ar1 + entry.getValue() + "";
							File f = new File(outFilePath);
							f.mkdir();
							clusterByDictionary.exportPdfDocument(outFilePath + "\\1. PMRA芯片疾病风险基因检测报告.pdf", resultMap, 0);
							clusterByDictionary.exportPdfDocument(outFilePath + "\\2. PMRA芯片药物风险基因检测报告.pdf", resultMap, 1);
							long endTime = System.currentTimeMillis();
							System.out.println("共耗时：" + (endTime - startTime) / 1000 / 60 + "min");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			fixedThreadPool.shutdown();
			while (true) {
				if (fixedThreadPool.isTerminated()) {
					System.out.println("线程池运行结束了！");
					break;
				}
				Thread.sleep(200);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static ArrayList createDictionaryList(String dictionaryFilePath) {
		int a = 1;
		ArrayList<Dictionary> dictionaryList = new ArrayList<Dictionary>();
		ArrayList<HashMap<String, String>> propertiesList = readRawFile(dictionaryFilePath);
		for (HashMap<String, String> hashMap : propertiesList) {
			// System.out.println(a++);
			Dictionary dictionary = new Dictionary(hashMap.get("recordname"), hashMap.get("recordmodel"),
					hashMap.get("recordeffect"), hashMap.get("rsid"), hashMap.get("genename"), hashMap.get("geno"),
					hashMap.get("catagrory"), hashMap.get("description"), hashMap.get("reference"),
					hashMap.get("recordlabel"), hashMap.get("diseaseCatagrory"));
			//System.out.println(dictionary.toString());
			dictionaryList.add(dictionary);
		}
		return dictionaryList;
	}

	/**
	 * 读取某个文件夹下的所有文件
	 */
	public static HashMap readfile(String filepath) throws FileNotFoundException, IOException {
		HashMap fileMap = null;
		try {
			fileMap = new HashMap<String, String>();
			File file = new File(filepath);
			if (!file.isDirectory()) {
				String fileName = file.getName();
				if (fileName.contains(".") && fileName.contains("_")) {
					fileName = fileName.substring(0, fileName.indexOf("."));
					fileName = fileName.substring(0, fileName.lastIndexOf("_"));
				} else if (fileName.contains(".txt")) {
					fileName = fileName.substring(0, fileName.indexOf(".txt"));
				}
				fileMap.put(file.getAbsolutePath(), fileName);
			} else if (file.isDirectory()) {
				System.out.println("文件夹");
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					if (!readfile.isDirectory()) {
						String fileName = readfile.getName();
						if (fileName.contains(".") && fileName.contains("_")) {
							fileName = fileName.substring(0, fileName.indexOf("."));
							fileName = fileName.substring(0, fileName.lastIndexOf("_"));
						} else if (fileName.contains(".txt")) {
							fileName = fileName.substring(0, fileName.indexOf(".txt"));
						}
						fileMap.put(readfile.getAbsolutePath(), fileName);
					} else if (readfile.isDirectory()) {
						readfile(filepath + "\\" + filelist[i]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
		return fileMap;
	}

	public static ArrayList readRawFile(String fileName) {
		File rawFile = new File(fileName);
		InputStream iStream = null;
		BufferedReader bufferedReader = null;
		ArrayList<String> rawLines = new ArrayList<String>();
		try {
			if (rawFile.isDirectory()) {
				System.out.println("您输入的为文件夹路径，请输入文件路径");
				return null;
			} else {
				iStream = new FileInputStream(rawFile);
				bufferedReader = new BufferedReader(new InputStreamReader(iStream));
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					rawLines.add(line);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		if (rawLines.size() < 2) {
			return null;
		}

		ArrayList<HashMap<String, String>> propertiesList = new ArrayList<HashMap<String, String>>();
		String[] keys = rawLines.get(0).split("\t");
		String[] values = null;
		String line = null;
		for (int i = 1; i < rawLines.size(); i++) {
			HashMap<String, String> properties = new LinkedHashMap<String, String>();
			line = rawLines.get(i);
			values = line.split("\t");
			for (int j = 0; j < values.length; j++) {
				properties.put(keys[j], values[j]);
			}
			propertiesList.add(properties);
		}

		return propertiesList;
	}

	/**
	 * 传入pdf输出路径及相关内容的map
	 * 
	 * @param outFilePath
	 *            产生的pdf的输出路径
	 * @param resultMap
	 *            与dictionary比对后分别用list保存查到的疾病和药物的dictionary对象：疾病的键-‘
	 *            GENETIC_DISEASE’，药物的键-‘MEDICINE_RELATION’
	 * @param pdfNum
	 *            0 代表产生疾病的结果报告，1代表产生药物的结果报告
	 */
	public static void exportPdfDocument(String outFilePath, Map resultMap, int pdfNum) {
		// Create Document Instance
		Document document = new Document();
		try {
			// add Chinese font
			BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			Font headfont = new Font(bfChinese, 20, Font.BOLD);
			Font subheadfont = new Font(bfChinese, 12, Font.BOLD);
			Font keyfont = new Font(bfChinese, 8, Font.BOLD);
			Font textfont = new Font(bfChinese, 8, Font.NORMAL);
			LinkedHashMap<String, Font> fontMap = new LinkedHashMap<String, Font>();
			fontMap.put("正常基因型", new Font(bfChinese, 8, Font.NORMAL));
			fontMap.put("有害突变携带者", new Font(bfChinese, 8, Font.NORMAL, Color.BLUE));
			fontMap.put("有害突变纯合", new Font(bfChinese, 8, Font.NORMAL, Color.RED));
			fontMap.put("携带2个有害突变", new Font(bfChinese, 8, Font.NORMAL, Color.ORANGE));
			// Create Writer associated with document
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(outFilePath)));

			document.open();
			document.newPage();
			// 添加pdf的标题
			Table header = new Table(1);
			header.setBorderColor(Color.WHITE);
			header.setWidth(100);
			Cell cheader = new Cell(new Paragraph(
					pdfNum == 0 ? printBlank(32) + "PMRA芯片疾病风险基因检测报告" : printBlank(32) + "PMRA芯片药物风险基因检测报告", headfont));
			header.addCell(cheader);
			document.add(header);
			Paragraph blankspace1 = new Paragraph(printBlank(134), keyfont);
			document.add(blankspace1);
			// 副标题或者附加信息
			Table tHeader = new Table(4);
			tHeader.setBorderColor(Color.WHITE);
			float[] widthsHeader = new float[4];
			widthsHeader[0] = 5f;
			widthsHeader[1] = 5f;
			widthsHeader[2] = 5f;
			widthsHeader[3] = 5f;
			tHeader.setWidths(widthsHeader);
			tHeader.setWidth(100);
			tHeader.getDefaultCell().setBorder(Cell.NO_BORDER);

			Cell c1Header = new Cell(new Paragraph(
					"ID：" + (resultMap.get("workerid") == null ? "____________" : resultMap.get("workerid")), keyfont));
			tHeader.addCell(c1Header);
			c1Header = new Cell(new Paragraph(
					"姓名：" + (resultMap.get("workername") == null ? "____________" : resultMap.get("workername")),
					keyfont));
			tHeader.addCell(c1Header);
			c1Header = new Cell(new Paragraph(
					"性别：" + (resultMap.get("workergender") == null ? "____________" : resultMap.get("workergender")),
					keyfont));
			tHeader.addCell(c1Header);
			c1Header = new Cell(new Paragraph(
					"年龄：" + (resultMap.get("workerage") == null ? "____________" : resultMap.get("workerage")),
					keyfont));
			tHeader.addCell(c1Header);
			document.add(tHeader);
			Paragraph blankspace3 = new Paragraph(printBlank(100), keyfont);
			document.add(blankspace3);
			// 判断生成疾病的pdf还是药物的pdf：0是疾病，1是药物
			if (pdfNum == 0) {// 疾病pdf
				// 为了使pdf中疾病的表格产生合并的效果，使用map来处理数据
				List<Dictionary> diseaseList = (List<Dictionary>) resultMap.get("GENETIC_DISEASE");
				LinkedHashMap<String, LinkedHashSet<Dictionary>> catagroryMap = new LinkedHashMap<String, LinkedHashSet<Dictionary>>();
				LinkedHashMap<String, LinkedHashSet<Dictionary>> recordnameMap = new LinkedHashMap<String, LinkedHashSet<Dictionary>>();
				LinkedHashMap<String, LinkedHashSet<String>> catagrorynameMap = new LinkedHashMap<String, LinkedHashSet<String>>();
				LinkedHashMap<String, String> recordnamemodelMap = new LinkedHashMap<String, String>();
				LinkedHashMap<String, LinkedHashSet<Dictionary>> catagroryMap1 = new LinkedHashMap<String, LinkedHashSet<Dictionary>>();
				LinkedHashMap<String, LinkedHashSet<Dictionary>> recordnameMap1 = new LinkedHashMap<String, LinkedHashSet<Dictionary>>();
				LinkedHashMap<String, LinkedHashSet<String>> catagrorynameMap1 = new LinkedHashMap<String, LinkedHashSet<String>>();
				LinkedHashMap<String, String> recordnamemodelMap1 = new LinkedHashMap<String, String>();
				int countNum = 0;
				Collections.sort(diseaseList);  
				for (Dictionary dictionary : diseaseList) {
					if (countNum <= 30) {
						if (catagroryMap.containsKey(dictionary.getDiseaseCatagrory())) {
							catagroryMap.get(dictionary.getDiseaseCatagrory()).add(dictionary);
						} else {
							LinkedHashSet newList = new LinkedHashSet();
							newList.add(dictionary);
							catagroryMap.put(dictionary.getDiseaseCatagrory(), newList);
						}
						if (catagrorynameMap.containsKey(dictionary.getDiseaseCatagrory())) {
							catagrorynameMap.get(dictionary.getDiseaseCatagrory()).add(dictionary.getRecordname());
						} else {
							LinkedHashSet newList2 = new LinkedHashSet();
							newList2.add(dictionary.getRecordname());
							catagrorynameMap.put(dictionary.getDiseaseCatagrory(), newList2);
						}
						if (recordnameMap.containsKey(dictionary.getRecordname())) {
							recordnameMap.get(dictionary.getRecordname()).add(dictionary);
						} else {
							LinkedHashSet newList1 = new LinkedHashSet();
							newList1.add(dictionary);
							recordnameMap.put(dictionary.getRecordname(), newList1);
						}
						if (recordnamemodelMap.containsKey(dictionary.getRecordname())) {

						} else {
							recordnamemodelMap.put(dictionary.getRecordname(), dictionary.getRecordmodel());
						}
					} else {
						if (catagroryMap1.containsKey(dictionary.getDiseaseCatagrory())) {
							catagroryMap1.get(dictionary.getDiseaseCatagrory()).add(dictionary);
						} else {
							LinkedHashSet newList = new LinkedHashSet();
							newList.add(dictionary);
							catagroryMap1.put(dictionary.getDiseaseCatagrory(), newList);
						}
						if (catagrorynameMap1.containsKey(dictionary.getDiseaseCatagrory())) {
							catagrorynameMap1.get(dictionary.getDiseaseCatagrory()).add(dictionary.getRecordname());
						} else {
							LinkedHashSet newList2 = new LinkedHashSet();
							newList2.add(dictionary.getRecordname());
							catagrorynameMap1.put(dictionary.getDiseaseCatagrory(), newList2);
						}
						if (recordnameMap1.containsKey(dictionary.getRecordname())) {
							recordnameMap1.get(dictionary.getRecordname()).add(dictionary);
						} else {
							LinkedHashSet newList1 = new LinkedHashSet();
							newList1.add(dictionary);
							recordnameMap1.put(dictionary.getRecordname(), newList1);
						}
						if (recordnamemodelMap1.containsKey(dictionary.getRecordname())) {

						} else {
							recordnamemodelMap1.put(dictionary.getRecordname(), dictionary.getRecordmodel());
						}
					}
					countNum++;
				}
				/*addDiseaseTableToDocument(document, catagroryMap, recordnameMap, catagrorynameMap, recordnamemodelMap,
						keyfont, textfont, fontMap);*/
				
				Table t = new Table(7);
				t.setBorderWidth(1); // 将边框宽度设为1
				t.setPadding(2);
				float[] widths = new float[7];
				widths[0] = 1.1f;
				widths[1] = 1.7f;
				widths[2] = 1.5f;
				widths[3] = 1.1f;
				widths[4] = 1f;
				widths[5] = 1f;
				widths[6] = 1f;
				t.setWidths(widths);
				t.setWidth(100);
				// t.getDefaultCell().setHorizontalAlignment(Cell.ALIGN_CENTER);
				Cell c1 = new Cell(new Paragraph("分类", keyfont));
				t.addCell(c1);
				c1 = new Cell(new Paragraph("疾病名称", keyfont));
				t.addCell(c1);
				c1 = new Cell(new Paragraph("遗传模式", keyfont));
				t.addCell(c1);
				c1 = new Cell(new Paragraph("描述", keyfont));
				t.addCell(c1);
				c1 = new Cell(new Paragraph("基因", keyfont));
				t.addCell(c1);
				c1 = new Cell(new Paragraph("SNP", keyfont));
				t.addCell(c1);
				c1 = new Cell(new Paragraph("基因型", keyfont));
				t.addCell(c1);
				for (Map.Entry<String, LinkedHashSet<Dictionary>> entry : catagroryMap.entrySet()) {
					Cell cell1 = new Cell(new Phrase(entry.getKey(), textfont));
					cell1.setRowspan(entry.getValue().size());
					cell1.setUseAscender(true); // 设置可以居中
					// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
					cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
					t.addCell(cell1);
					for (String recordname : catagrorynameMap.get(entry.getKey())) {
						cell1 = new Cell(new Phrase(recordname, textfont));
						cell1.setRowspan(recordnameMap.get(recordname).size());
						cell1.setUseAscender(true); // 设置可以居中
						// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
						cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
						t.addCell(cell1);
						cell1 = new Cell(new Phrase(recordnamemodelMap.get(recordname), textfont));
						cell1.setRowspan(recordnameMap.get(recordname).size());
						cell1.setUseAscender(true); // 设置可以居中
						// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
						cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
						t.addCell(cell1);
						for (Dictionary dic : recordnameMap.get(recordname)) {
							cell1 = new Cell(new Phrase(dic.catagrory, fontMap.get(dic.catagrory)));
							t.addCell(cell1);
							cell1 = new Cell(new Phrase(dic.genename, textfont));
							t.addCell(cell1);
							cell1 = new Cell(new Phrase(dic.rsid, textfont));
							t.addCell(cell1);
							cell1 = new Cell(new Phrase(dic.geno, textfont));
							t.addCell(cell1);
						}
					}
				}

				document.add(t);
				
				if (catagroryMap1.size() > 0) {
					document.newPage();
					/*addDiseaseTableToDocument(document, catagroryMap1, recordnameMap1, catagrorynameMap1,
							recordnamemodelMap1, keyfont, textfont, fontMap);*/
					Table tt = new Table(7);
					tt.setBorderWidth(1); // 将边框宽度设为1
					tt.setPadding(2);
					float[] widthst = new float[7];
					widthst[0] = 1.1f;
					widthst[1] = 1.7f;
					widthst[2] = 1.5f;
					widthst[3] = 1.1f;
					widthst[4] = 1f;
					widthst[5] = 1f;
					widthst[6] = 1f;
					tt.setWidths(widthst);
					tt.setWidth(100);
					// t.getDefaultCell().setHorizontalAlignment(Cell.ALIGN_CENTER);
					Cell c1t = new Cell(new Paragraph("分类", keyfont));
					tt.addCell(c1t);
					c1t = new Cell(new Paragraph("疾病名称", keyfont));
					tt.addCell(c1t);
					c1t = new Cell(new Paragraph("遗传模式", keyfont));
					tt.addCell(c1t);
					c1t = new Cell(new Paragraph("描述", keyfont));
					tt.addCell(c1t);
					c1t = new Cell(new Paragraph("基因", keyfont));
					tt.addCell(c1t);
					c1t = new Cell(new Paragraph("SNP", keyfont));
					tt.addCell(c1t);
					c1t = new Cell(new Paragraph("基因型", keyfont));
					tt.addCell(c1t);
					for (Map.Entry<String, LinkedHashSet<Dictionary>> entry : catagroryMap1.entrySet()) {
						Cell cell1 = new Cell(new Phrase(entry.getKey(), textfont));
						cell1.setRowspan(entry.getValue().size());
						cell1.setUseAscender(true); // 设置可以居中
						// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
						cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
						tt.addCell(cell1);
						for (String recordname : catagrorynameMap1.get(entry.getKey())) {
							cell1 = new Cell(new Phrase(recordname, textfont));
							cell1.setRowspan(recordnameMap1.get(recordname).size());
							cell1.setUseAscender(true); // 设置可以居中
							// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
							cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
							tt.addCell(cell1);
							cell1 = new Cell(new Phrase(recordnamemodelMap1.get(recordname), textfont));
							cell1.setRowspan(recordnameMap1.get(recordname).size());
							cell1.setUseAscender(true); // 设置可以居中
							// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
							cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
							tt.addCell(cell1);
							for (Dictionary dic : recordnameMap1.get(recordname)) {
								cell1 = new Cell(new Phrase(dic.catagrory, fontMap.get(dic.catagrory)));
								tt.addCell(cell1);
								cell1 = new Cell(new Phrase(dic.genename, textfont));
								tt.addCell(cell1);
								cell1 = new Cell(new Phrase(dic.rsid, textfont));
								tt.addCell(cell1);
								cell1 = new Cell(new Phrase(dic.geno, textfont));
								tt.addCell(cell1);
							}
						}
					}

					document.add(tt);
					
				}
			}
			if (pdfNum == 1) {// 药物pdf

				List<String> ponum1 = new ArrayList<String>();
				List<String> line1 = new ArrayList<String>();
				List<String> part1 = new ArrayList<String>();
				List<String> description1 = new ArrayList<String>();
				List<String> origin1 = new ArrayList<String>();
				List<String> comment1 = new ArrayList<String>();
				List<Dictionary> drugList = (List<Dictionary>) resultMap.get("MEDICINE_RELATION");
				for (Dictionary dictionary : drugList) {
					ponum1.add(dictionary.recordname);
					line1.add(dictionary.recordeffect);
					part1.add(dictionary.genename);
					description1.add(dictionary.rsid);
					origin1.add(dictionary.geno);
					comment1.add(dictionary.description);
				}
				/*addDrugTableToDocument(document, drugList, ponum1, line1, part1, description1, origin1, comment1,
						keyfont, textfont);*/
				Table t1 = new Table(6);
				t1.setPadding(2);
				// t1.getDefaultCell().setHorizontalAlignment(Cell.ALIGN_CENTER);
				float[] widths1 = new float[6];
				widths1[0] = 1.2f;
				widths1[1] = 1.2f;
				widths1[2] = 3.6f;
				widths1[3] = 0.9f;
				widths1[4] = 0.9f;
				widths1[5] = 0.9f;
				t1.setWidths(widths1);
				t1.setWidth(100);
				t1.getDefaultCell().setBorder(Cell.TABLE);
				t1.setAlignment(Table.ALIGN_LEFT);
				Cell c2 = new Cell(new Paragraph("药物", keyfont));
				t1.addCell(c2);
				c2 = new Cell(new Paragraph("治疗作用", keyfont));
				t1.addCell(c2);
				c2 = new Cell(new Paragraph("描述", keyfont));
				t1.addCell(c2);
				c2 = new Cell(new Paragraph("基因", keyfont));
				t1.addCell(c2);
				c2 = new Cell(new Paragraph("SNP", keyfont));
				t1.addCell(c2);
				c2 = new Cell(new Paragraph("基因型", keyfont));
				t1.addCell(c2);
				for (int i = 0; i < drugList.size(); i++) {
					Cell c3 = new Cell(new Paragraph(ponum1.get(i), textfont));
					t1.addCell(c3);
					c3 = new Cell(new Paragraph(line1.get(i), textfont));
					t1.addCell(c3);
					c3 = new Cell(new Paragraph(comment1.get(i), textfont));
					t1.addCell(c3);
					c3 = new Cell(new Paragraph(part1.get(i), textfont));
					t1.addCell(c3);
					c3 = new Cell(new Paragraph(description1.get(i), textfont));
					t1.addCell(c3);
					c3 = new Cell(new Paragraph(origin1.get(i), textfont));
					t1.addCell(c3);
				}
				document.add(t1);
				
				
			}

			// pdf的备注信息：报告日期和备注
			Paragraph foot11 = new Paragraph(printBlank(250) + "报告日期：" + getCurrentDateString(), keyfont);
			document.add(foot11);
			PdfContentByte canvas = writer.getDirectContent();
			Phrase phrase1 = new Phrase("检测局限性：该检测⽆法覆盖所有与疾病⻛险及⽤药⻛险相关的位点，此结果并不能完全排除" + "受检者罹患疾病的⻛险或潜在的⽤药⻛险，不排除",
					textfont);
			Phrase phrase2 = new Phrase("在检测基因范围以外存在其他未知致病突变的可能，该结果不能代替临床诊断意见。", textfont);
			Phrase phrase3 = new Phrase("___________________________________________________", headfont);
			Phrase phrase4 = new Phrase("___________________________________________________", headfont);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase1, 35, 55, 0);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase2, 35, 45, 0);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase3, 35, 40, 0);
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, phrase4, 35, 70, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		document.close();
	}

	/**
	 * 往document中增加table
	 * 
	 * @throws DocumentException
	 */
	public static void addDiseaseTableToDocument(Document document,
			LinkedHashMap<String, LinkedHashSet<Dictionary>> catagroryMap,
			LinkedHashMap<String, LinkedHashSet<Dictionary>> recordnameMap,
			LinkedHashMap<String, LinkedHashSet<String>> catagrorynameMap,
			LinkedHashMap<String, String> recordnamemodelMap, Font keyfont, Font textfont,
			LinkedHashMap<String, Font> fontMap) throws DocumentException {
		Table t = new Table(7);
		t.setBorderWidth(1); // 将边框宽度设为1
		t.setPadding(2);
		float[] widths = new float[7];
		widths[0] = 1.1f;
		widths[1] = 1.7f;
		widths[2] = 1.5f;
		widths[3] = 1.1f;
		widths[4] = 1f;
		widths[5] = 1f;
		widths[6] = 1f;
		t.setWidths(widths);
		t.setWidth(100);
		// t.getDefaultCell().setHorizontalAlignment(Cell.ALIGN_CENTER);
		Cell c1 = new Cell(new Paragraph("分类", keyfont));
		t.addCell(c1);
		c1 = new Cell(new Paragraph("疾病名称", keyfont));
		t.addCell(c1);
		c1 = new Cell(new Paragraph("遗传模式", keyfont));
		t.addCell(c1);
		c1 = new Cell(new Paragraph("描述", keyfont));
		t.addCell(c1);
		c1 = new Cell(new Paragraph("基因", keyfont));
		t.addCell(c1);
		c1 = new Cell(new Paragraph("SNP", keyfont));
		t.addCell(c1);
		c1 = new Cell(new Paragraph("基因型", keyfont));
		t.addCell(c1);
		for (Map.Entry<String, LinkedHashSet<Dictionary>> entry : catagroryMap.entrySet()) {
			Cell cell1 = new Cell(new Phrase(entry.getKey(), textfont));
			cell1.setRowspan(entry.getValue().size());
			cell1.setUseAscender(true); // 设置可以居中
			// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
			cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
			t.addCell(cell1);
			for (String recordname : catagrorynameMap.get(entry.getKey())) {
				cell1 = new Cell(new Phrase(recordname, textfont));
				cell1.setRowspan(recordnameMap.get(recordname).size());
				cell1.setUseAscender(true); // 设置可以居中
				// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
				cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
				t.addCell(cell1);
				cell1 = new Cell(new Phrase(recordnamemodelMap.get(recordname), textfont));
				cell1.setRowspan(recordnameMap.get(recordname).size());
				cell1.setUseAscender(true); // 设置可以居中
				// cell1.setHorizontalAlignment(Cell.ALIGN_CENTER); //设置水平居中
				cell1.setVerticalAlignment(Cell.ALIGN_MIDDLE); // 设置垂直居中
				t.addCell(cell1);
				for (Dictionary dic : recordnameMap.get(recordname)) {
					cell1 = new Cell(new Phrase(dic.catagrory, fontMap.get(dic.catagrory)));
					t.addCell(cell1);
					cell1 = new Cell(new Phrase(dic.genename, textfont));
					t.addCell(cell1);
					cell1 = new Cell(new Phrase(dic.rsid, textfont));
					t.addCell(cell1);
					cell1 = new Cell(new Phrase(dic.geno, textfont));
					t.addCell(cell1);
				}
			}
		}

		document.add(t);
	}

	/**
	 * 往document中增加table
	 * 
	 * @throws DocumentException
	 */
	public static void addDrugTableToDocument(Document document, List<Dictionary> drugList, List<String> ponum1,
			List<String> line1, List<String> part1, List<String> description1, List<String> origin1,
			List<String> comment1, Font keyfont, Font textfont) throws DocumentException {
		Table t1 = new Table(6);
		t1.setPadding(2);
		// t1.getDefaultCell().setHorizontalAlignment(Cell.ALIGN_CENTER);
		float[] widths1 = new float[6];
		widths1[0] = 1.2f;
		widths1[1] = 1.2f;
		widths1[2] = 3.6f;
		widths1[3] = 0.9f;
		widths1[4] = 0.9f;
		widths1[5] = 0.9f;
		t1.setWidths(widths1);
		t1.setWidth(100);
		t1.getDefaultCell().setBorder(Cell.TABLE);
		t1.setAlignment(Table.ALIGN_LEFT);
		Cell c2 = new Cell(new Paragraph("药物", keyfont));
		t1.addCell(c2);
		c2 = new Cell(new Paragraph("治疗作用", keyfont));
		t1.addCell(c2);
		c2 = new Cell(new Paragraph("描述", keyfont));
		t1.addCell(c2);
		c2 = new Cell(new Paragraph("基因", keyfont));
		t1.addCell(c2);
		c2 = new Cell(new Paragraph("SNP", keyfont));
		t1.addCell(c2);
		c2 = new Cell(new Paragraph("基因型", keyfont));
		t1.addCell(c2);
		for (int i = 0; i < drugList.size(); i++) {
			Cell c3 = new Cell(new Paragraph(ponum1.get(i), textfont));
			t1.addCell(c3);
			c3 = new Cell(new Paragraph(line1.get(i), textfont));
			t1.addCell(c3);
			c3 = new Cell(new Paragraph(comment1.get(i), textfont));
			t1.addCell(c3);
			c3 = new Cell(new Paragraph(part1.get(i), textfont));
			t1.addCell(c3);
			c3 = new Cell(new Paragraph(description1.get(i), textfont));
			t1.addCell(c3);
			c3 = new Cell(new Paragraph(origin1.get(i), textfont));
			t1.addCell(c3);
		}
		document.add(t1);
	}

	/**
	 * 生成pdf时使用
	 * 
	 * @param tmp
	 * @return
	 */
	public static String printBlank(int tmp) {
		String space = "";
		for (int m = 0; m < tmp; m++) {
			space = space + " ";
		}
		return space;
	}

	static SimpleDateFormat dateParser1 = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat dateParser2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static Date parseSimpleDate(String simpleDate) throws ParseException {
		return dateParser1.parse(simpleDate);
	}

	public static Date parseSimpleDateTime(String simpleDateTime) throws ParseException {
		return dateParser2.parse(simpleDateTime);
	}

	public static String getCurrentTimeString() throws ParseException {
		long timestamp = System.currentTimeMillis();
		return dateParser2.format(new Date(timestamp));
	}

	public static String getCurrentDateString() throws ParseException {
		long timestamp = System.currentTimeMillis();
		return dateParser1.format(new Date(timestamp));
	}
}
