package qy.test;

public class BlogServiceImpl implements BlogService {
    @Override
    public String getName(int id) {
        return "blog" + id;
    }
}
