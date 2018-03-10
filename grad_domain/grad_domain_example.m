% The test image is 16-bit tiff
bit_depth = 16;
img_rgb = double(imread( 'test_image-1.tif' ))/(2^bit_depth-1);

% Compute the gray-scale as the mean or red, green and blue
img_l = mean( img_rgb, 3 );

% Image size
sz = [size(img_rgb,1) size(img_rgb,2)];

% Gradient field
G = get_img_grad( img_l );

Gm = sqrt( sum(G.^2, 3) );  % Gradient magnitudes

% A simple modification of gradient values (piece-wise linear function)
Gm_new = interp1( [0 0.01 0.1 1.0], ...
                  [0 0.01 0.2 1.0], Gm ); 

r = Gm_new./Gm;   
r(isnan(r)) = 0;  % For the cases when Gm==0

G = G .* repmat( r, [1 1 2] );

W = ones(sz); % Do not use weights - set them to 1

[A, b] = create_grad_Ab( sz, G, W );

display( 'Started the solver' )
tic;
img_l_new = A\b;
toc
display( 'The solved has finished' )

% From a column vector to a 2D image
img_l_new = reshape(img_l_new,sz);

% Because the reconstructed pixel values are relative, we make sure that
% the mean values = 0.5.
img_l_new = img_l_new - mean(img_l_new(:)) + 0.5;

% Transfer colours from the original image
img_rgb_res = img_rgb .* repmat( img_l_new ./ img_l, [1 1 3] );

clf;
imshow( img_rgb_res );
%pfsview( img_rgb_res );



