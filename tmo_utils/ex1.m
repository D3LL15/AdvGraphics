img = hdrread('memorial.hdr');

L = clamp((img ./ 100) .^ 1, 0, 1);

rgb = lin2srgb(L);

imtool(rgb);