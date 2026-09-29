import { PageResponse } from '../../shared/page';
import { FormsQuery } from './apply-requests';
import {
  ApplicationDetailResponse,
  ApplicationSummaryResponse,
} from './apply-responses';

export abstract class ApplicationFinder {
  /** A null decision filter matches undecided applications (IS NULL). */
  abstract list(
    query: FormsQuery,
  ): Promise<PageResponse<ApplicationSummaryResponse>>;
  abstract detail(id: string): Promise<ApplicationDetailResponse>;
}
