package cn.smbms.tools;

import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateConverter implements Converter<String, Date> {
    private String datePatter;

    public StringToDateConverter(String datePatter) {
        this.datePatter = datePatter;
    }

    @Override
    public Date convert(String source) {
        Date date=null;
        try {
            date =new SimpleDateFormat(datePatter).parse(source);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
