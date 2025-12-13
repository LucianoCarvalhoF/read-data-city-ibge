package com.ifpb.read_data_city_ibge.services;

import com.ifpb.read_data_city_ibge.models.ReadDataRequestModel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
public class IbgeHtmlReadService {

    @SuppressWarnings("unused")
    @Value("${ibge.data.url.prefix}")
    private String ibgeUrlPrefix;

    public String readData(ReadDataRequestModel requestModel) {
        List<String> result = new ArrayList<>();
        int year = requestModel.getYear();

        try {
            String url = ibgeUrlPrefix + year;
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("table tbody tr td:nth-child(2) a");

            Optional<Element> element = elements.stream().filter(filterElements()).findFirst();

            if (element.isPresent()) {
                result.add(String.format(url + "/%s", element.get().attr("href")));
            } else {
                log.error("No elements found for the given CSS selector.");
            }
        } catch (IOException e) {
            log.error("Error fetching or parsing HTML. Details: {}", e.getMessage());
        }

        return result.get(0);
    }

    private static Predicate<Element> filterElements() {
        return e -> e.text().contains(".xls");
    }
}
