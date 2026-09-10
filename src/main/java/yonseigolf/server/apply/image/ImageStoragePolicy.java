package yonseigolf.server.apply.image;

public record ImageStoragePolicy(
        boolean publicReadAcl,
        boolean includeBucketInPublicUrl
) {
}
