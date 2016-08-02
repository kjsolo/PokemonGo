package cn.soloho.pokemongo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by solo on 16/8/1.
 */
public class DataManager {

    private static final String TAG = "DataManager";

    private static DataManager ourInstance = new DataManager();

    public static DataManager getInstance() {
        return ourInstance;
    }

    private DataManager() {
    }

    public Observable<List<DocumentModel>> getPokemonDocs() {
        return Observable.create(new Observable.OnSubscribe<List<DocumentModel>>() {
            @Override
            public void call(Subscriber<? super List<DocumentModel>> subscriber) {
                try {
                    Document doc = Jsoup.connect("http://www.entertainment14.net/blog/post/110938688-pokemon-go-%E6%94%BB%E7%95%A5%E5%8C%AF%E9%9B%86").get();
                    Elements eLinks = doc.select("div.entry-content a");
                    List<DocumentModel> documentModels = new ArrayList<>();
                    for (Element eLink : eLinks) {
                        String link = eLink.attr("href");
                        String title = eLink.text();
                        DocumentModel documentModel = DocumentModel.builder()
                                .setTitle(title)
                                .setLink(link)
                                .build();
                        documentModels.add(documentModel);
                    }
                    subscriber.onNext(documentModels);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<String> getPokemonDetail(final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    Document doc = Jsoup.connect(url).get();
                    doc.select("div.entry-content noscript").remove();
                    Elements eImgs = doc.select("div.entry-content img");
                    for (Element eImg : eImgs) {
                        String originalSrc = eImg.attr("data-lazy-src");
                        eImg.attr("src", originalSrc);
                        eImg.removeAttr("data-lazy-src");
                    }
                    Elements eBody = doc.select("div.entry-content");
                    subscriber.onNext(eBody.html());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }
}
