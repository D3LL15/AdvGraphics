function [A, b] = create_grad_Ab( sz, G, w )
% Create a sparse linear system Ax=b for reconstructing an image from a
% gradient field
%
% [A, b] = create_grad_Ab( sz, Gx, Gy, w )
%
% sz - image size as [rows cols]
% G - a gradient field crated with get_img_grad
% w - matrix of the same size as the image, assigning a weight to each
%     corresponding gradient
%
% A - a sparse matrix
% b - a column vector
%
% The resulting image can be found by a sparse solver: Y = A\B;
% Refer to the example.


N = prod(sz);
w = w(:);
assert( numel(w) == N );

B = cat( 2, -w, cat( 1, zeros(sz(1),1), w(1:(end-sz(1))) ) );
B((N-sz(1)+1:N),1) = 0;
Ogxf = spdiags( B, [0 sz(1)], N, N );  % forward difference operaror (along X), premultiplied with the weights

B = cat( 2, -w, cat( 1, 0, w(1:end-1) ) );
B(sz(1):sz(1):end,1) = 0;
B(sz(1)+1:sz(1):end,2) = 0;
Ogyf = spdiags( B, [0 1], N, N ); % forward difference operaror (along Y), premultiplied with the weights

A = Ogxf'*Ogxf + Ogyf'*Ogyf;

b = Ogxf' * (w.* reshape(G(:,:,1),[N 1])) + Ogyf' * (w.*reshape(G(:,:,2),[N 1]));

end
