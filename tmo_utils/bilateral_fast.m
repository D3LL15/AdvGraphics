function Y = bilateral_fast( X, sigma_s, sigma_r, n )
% Fast bilateral filter
%
% Y = bilateral_fast( X, sigma_s, sigma_r )
%
% X - input image
% Y - blurred image
% sigma_s - standard deviation of the spatial Gaussian kernel
% sigma_r - standard deviation of the range Gaussian kernel
% n - number of layers to use (the higher the number, the more accurate is
% the filter but it takes more time).
%
% Based on the algoritm from:
%
% Durand, F., & Dorsey, J. (2002). Fast bilateral filtering for the
% display of high-dynamic-range images. ACM Transactions on Graphics, 21(3). doi:10.1145/566654.566574
%
% (c) 2012 Rafal Mantiuk

if( size( X, 3 ) ~= 1 )
    error( 'bilateral_fast can process only grayscale images' );
end

if( ~exist( 'n', 'var' ) )
    n=6; % number of layers
end

min_x = min(X(:)); %prctile( X(:), 1 );
max_x = max(X(:)); %prctile( X(:), 99 );

r = linspace( min_x, max_x, n );

L = zeros( n, numel( X ) );

for i=1:n
    D = exp(-(X - r(i)).^2/(2*sigma_r^2));
    K = blur_gaussian( D, sigma_s );
    Ls = blur_gaussian( X.*D, sigma_s );    
    L(i,:) = Ls(:)./K(:);
end

% interpolate
ind_r = clamp((X(:)-min_x)/(max_x-min_x)*(n-1)+1, 1, n);
ind_down = floor(ind_r);
ind_up = ceil(ind_r);
ind_fix = (0:n:((numel(X)-1)*n))';
ind_up = ind_up + ind_fix;
ind_down = ind_down + ind_fix;
ratio = mod( ind_r, 1 );

Y = zeros( size(X) );
Y(:) = L(ind_up).*ratio + L(ind_down).*(1-ratio);

end


function Y = blur_gaussian( X, sigma )
% Low-pass filter image using the Gaussian filter
% 
% Y = blur_gaussian( X, sigma )
%  

ksize = ceil(sigma*6);
h = fspecial( 'gaussian', ksize, sigma );
Y = imfilter( X, h, 'replicate' );

end