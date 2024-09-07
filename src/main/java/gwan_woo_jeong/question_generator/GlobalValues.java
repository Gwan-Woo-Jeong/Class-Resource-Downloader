package gwan_woo_jeong.question_generator;

public class GlobalValues {
	public final static String introduction = "***시작 전 모든 설정을 본인 환경에 맞게 설정해주세요***\n" +
			"\n" +
			"  1. 사이트 URL: 리소스를 다운 받을 사이트 주소\n" +
			"\n" +
			"  2. 문제 폴더 경로: 문제 폴더/파일을 저장하는 최상위 폴더\n" +
			"\n" +
			"  3. 문제 폴더명 형식: 생성할 문제 폴더명의 형식 (String.format 형식)\n" +
			"\n" +
			"  * 현재 배우는 과목에 따라 class 사이트 구조가 달라져 정상적으로 작동하지 않을 수 있습니다.\n" +
			"     Version 1.2.0 (release 24-09-07)\n";

	public final static String defaultTargetUrl = "http://pinnpublic.dothome.co.kr/";
	public final static String defaultFileDirPath = "C:\\class\\code\\client\\ClientTest\\src\\main\\webapp\\html\\question";
	public final static String defaultFileNameFormat = "Q%03d";

	public static String targetUrl;
	public static String fileDirPath;
	public static String fileNameFormat; // ex) Q002, Q057, Q112

	public static void setValues (GeneratorView view) {
		GlobalValues.targetUrl = view.getTargetUrlField().getText();
		GlobalValues.fileDirPath = view.getFileDirPathField().getText();
		GlobalValues.fileNameFormat = view.getFileNameFormatField().getText();
		}
}
