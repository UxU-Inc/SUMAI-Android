package co.kr.sumai;

public class SummaryRequest {

    private String data;
    private String id;
    private int record;

    public SummaryRequest(String data, String id, int record) {
        this.data = data;
        this.id = id;
        this.record = record;
    }

}
