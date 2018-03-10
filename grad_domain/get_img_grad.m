function [G] = get_img_grad( img )
% Return a gradient field for a given grayscale image
% G(:,:,1) contains a gradient with respect to X (columns)
% G(:,:,2) contains a gradient with respect to Y (rows)

% We pad with the same values to ensure that Gx, Gy at the right and bottom
% edge is 0
img_padded = padarray( img, [1 1], 'replicate' );

G = zeros( size(img,1), size(img,2), 2 );
G(:,:,1) = conv2( img_padded(2:(end-1),:), fliplr([0 -1 1]), 'valid' );
G(:,:,2) = conv2( img_padded(:,2:(end-1)), fliplr([0 -1 1])', 'valid' );

end
