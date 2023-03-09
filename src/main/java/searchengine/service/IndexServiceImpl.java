package searchengine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.respository.IndexRepository;
import searchengine.util.texts.TextUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class IndexServiceImpl implements IndexService {
    private final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;
    private final PageService pageService;
    @Autowired
    public IndexServiceImpl(IndexRepository indexRepository, LemmaService lemmaService, PageService pageService) {
        this.indexRepository = indexRepository;
        this.lemmaService = lemmaService;
        this.pageService = pageService;
    }
    @Override
    public void add(Index index) {
        indexRepository.save(index);
    }

    @Override
    public boolean indexing(String url) {
        Optional<Page> optionalPage = Optional.ofNullable(pageService.getByUrl(url));
        Page page = optionalPage.orElseThrow(() -> new IllegalArgumentException("Данная страница находится за пределами сайтов," +
                "указанных в конфигурационном файле"));

        List<Index> indices = indexRepository.getAllByPageId(page.getId());
        if (!indices.isEmpty()) {
            return true;
        }

        Site site = page.getSite();
        try {
            saveIndexes(page, site);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return true;
    }

    private void saveIndexes(Page page, Site site) throws IOException {
        TextUtil textUtil = new TextUtil(page.getContent());
        LinkedHashMap<String, Integer> words = textUtil.countWords();
        for (var entry : words.entrySet()) {
            String word = entry.getKey();
            Optional<Lemma> optionalLemma = site.getLemmas().stream().filter(x -> x.getLemma().equals(word)).findFirst();
            Lemma lemma = optionalLemma.orElseGet(() -> new Lemma(site, word, 1));
            lemma.setFrequency(lemma.getFrequency() + entry.getValue());
            lemmaService.add(lemma);
            add(new Index(page, lemma, entry.getValue()));
        }
    }

    @Override
    public List<Index> getAllByLemmaId(int id){
        return indexRepository.getAllByLemmaId(id);
    }
}
